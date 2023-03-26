# 手写一个mini dubbo，彻底搞懂dubbo的基本原理

1. 首先我认为dubbo帮助我们解决了，微服务下应用与应用之间调用的复杂问题。
2. 如果没有dubbo，微服务这种架构还能实现吗？答案是肯定的，我们可以用http的形式，在每一个需要调用外部接口的地方，都采用http调用，并在provider中将接口通过http的形式暴露给外部系统。这样也能实现微服务之间的调用
3. 但是随之而来的就是大量的重复代码，大量的http调用，十分麻烦。
4. 因此dubbo应运而生。

---

## 我自己手写的dubbo是一套简单基于http和springboot的rpc远程方法调用的开箱即用的框架，并使用zookeeper作为注册中心

1. 能实现负载均衡
2. 能实现服务的自动注册，以及下线
3. 能实现消费者像调用**本地方法**一样调用**远程方法**

---

下面向大家介绍各个模块的作用

1. dubbo-core是核心包，实现了rpc远程调用
2. dubbo-registry是注册中心包，主要负责服务的注册，以及本地服务缓存的操作等
3. dubbo-domain是实体类包
4. dubbo-demo为demo

默认的provider:  zookeeper路径为 /frank/dubbo/provider/{interfaceName}/{url}

默认的consumer:  zookeeper路径为 /frank/dubbo/consumer/{interfaceName}/{url}

---

最核心的可能就是两个注解以及两个注解的核心实现

```java
/**
 * 将对应的类的所有接口，暴露给外部系统，供rpc调用
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface FrankDubbo {
}

/**
 * 依赖注入远程调用的代理类
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface FrankDubboReference {
}
```

---

## 提供者

向注册中心注册provider

```java
/**
 * 向注册中心注册provider
 */
@Component
public class RegisterProvider implements BeanPostProcessor {

    @Autowired
    private Registry providerZookeeperRegistry;

    @Value("${server.port}")
    private String port;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        // 判断bean上是否有FrankDubbo注解
        if (beanClass.isAnnotationPresent(FrankDubbo.class)) {
            // 获取当前bean的所有接口
            Class<?>[] interfaces = beanClass.getInterfaces();
            // 将所有的接口都暴露给外部系统
            Arrays.stream(interfaces).forEach(i -> {
                // 注册到本地缓存
                // 接口名，对应的bean实现，以及bean的名字
                LocalProviderCache.register(i.getName(), beanClass, beanName);
                String hostAddress = "";
                try {
                    // 获取当前服务起的ip地址
                    hostAddress = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }

                try {
                    // 注册到zookeeper中，接口名，url
                    providerZookeeperRegistry.register(i.getName(), new Url(hostAddress, port));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return bean;
    }
}
```

provider的请求分发

```java
/**
 * provider的请求分发
 */
@RestController
@Slf4j
public class ProviderMethodInvocation {

    @PostMapping(UrlConstant.RPC_URL)
    public Object dubbo(@RequestBody Invocation invocation) throws Exception {
        // 根据invocation 从本地缓存中获取当前接口的实现类
        ObjectInfo objectInfo = LocalProviderCache.get(invocation.getInterfaceName());
        if (Objects.isNull(objectInfo)) {
            log.error("interface未暴露到frank mini dubbo rpc调用中，interfaceName: {}", invocation.getInterfaceName());
            throw new ApiErrCodeException(ApiErrCodeExceptionEnum.INTERFACE_NOT_EXPORT);
        }
        log.info("执行{}.{}", invocation.getInterfaceName(), invocation.getMethodName());
        // 获取当前接口实现类的class实现
        Class clazz = objectInfo.getClazz();
        Method method = null;
        try {
            // 获取当前接口实现类的方法
            method = clazz.getMethod(invocation.getMethodName(), invocation.getParamTypes());
        } catch (NoSuchMethodException exception) {
            log.error("{}.{}方法未暴露到frank mini dubbo rpc调用中", invocation.getInterfaceName(), invocation.getMethodName());
            throw new ApiErrCodeException(ApiErrCodeExceptionEnum.INTERFACE_NOT_EXPORT);
        }
        if (Objects.isNull(method)) {
            log.error("{}.{}方法未暴露到frank mini dubbo rpc调用中", invocation.getInterfaceName(), invocation.getMethodName());
            throw new ApiErrCodeException(ApiErrCodeExceptionEnum.INTERFACE_NOT_EXPORT);
        }
        // 从spring容器中，获取当前实现类的bean
        Object bean = ApplicationContextUtil.getSpringBeanByTypeAndId(objectInfo.getBeanName(), clazz);
        if (Objects.isNull(bean)) {
            log.error("接口: {}, beanName: {}, 未能在spring容器中找到", invocation.getInterfaceName(), objectInfo.getBeanName());
            throw new ApiErrCodeException(ApiErrCodeExceptionEnum.OBJECT_NOT_IN_SPRING);
        }
        // 调用方法
        Object result = method.invoke(bean, invocation.getParams());
        log.info("result:{}", JSON.toJSONString(result));
        return result;
    }
}
```

---

## 消费者

向注册中心注册消费者，这一步对于整个系统的调用没有实质作用，但是可以便于维护。可以看到，每个接口的实际调用者

并获取provider

以及监听注册中心中当前接口provider的变化

```java
/**
 * 向注册中心注册消费者，这一步对于整个系统的调用没有实质作用，但是可以便于维护。可以看到，每个接口的实际调用者
 * 并获取provider
 * 以及监听注册中心中当前接口provider的变化
 */
@Component
public class RegisterConsumer implements BeanPostProcessor {

    @Autowired
    private Registry providerZookeeperRegistry;

    @Autowired
    private Registry consumerZookeeperRegistry;

    @Value("${server.port}")
    private String port;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取当前bean的class
        Class<?> beanClass = bean.getClass();
        // 获取当前bean的所有对象
        Field[] fields = beanClass.getDeclaredFields();
        String hostAddress = "";
        try {
            // 获取当前服务器的ip地址
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String finalHostAddress = hostAddress;
        // 遍历每个对象，将其注入到本地缓存和注册中心中，并获取provider，以及监听当前接口
        Arrays.stream(fields).forEach(field -> {
            // 判断对象上是否有FrankDubboReference注解
            if (field.isAnnotationPresent(FrankDubboReference.class)) {
                try {
                    // 将当前调用方注册到注册中心
                    consumerZookeeperRegistry.register(field.getType().getName(), new Url(finalHostAddress, port));
                    // 获取当前接口的所有provider，并将它存储到本地缓存中，interfaceName，List<Url>
                    providerZookeeperRegistry.getUrls(field.getType().getName());
                    // 监听当前接口的注册中心变化，根据注册中心变化，实时更新本地缓存
                    providerZookeeperRegistry.watchInterface(field.getType().getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return bean;
    }
}
```

将代理类注入到打了FrankDubboReference注解的对象上

```java
/**
 * 将代理类注入到打了FrankDubboReference注解的对象上
 */
@Component
@Slf4j
public class ConsumerProxy implements BeanPostProcessor {

    @Autowired
    private LoadBalance loadBalance;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            if (field.isAnnotationPresent(FrankDubboReference.class)) {
                field.setAccessible(true);
                try {
                    // 核心方法，将代理类注入到打了FrankDubboReference注解的对象上
                    field.set(bean, getProxy(field.getType()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return bean;
    }

    private Object getProxy(Class interfaceClass) {
        return Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass},
                (proxy, method, args) -> {
                    // 构建一个对象，包含接口名，方法名，方法参数，方法参数类型
                    Invocation invocation = new Invocation(interfaceClass.getName(), method.getName(), args, method.getParameterTypes());
                    // 从本地缓存中获取，当前接口的所有providers
                    List<Url> urls = LocalConsumerCache.get(interfaceClass.getName());
                    if (CollectionUtils.isEmpty(urls)) {
                        log.error("当前接口: {}，没有存活的提供者: {}", interfaceClass.getName(), urls);
                        throw new ApiErrCodeException(NO_ALIVE_PROVIDER);
                    }
                    log.debug("当前接口: {}，存活的提供者: {}", interfaceClass.getName(), urls.toString());
                    // 根据负载均衡策略获取对应的url
                    Url url = loadBalance.getUrl(urls);
                    log.debug("当前接口: {}，选择: {}", interfaceClass.getName(), url);
                    // 使用http请求，远程调用当前接口
                    String result = HttpUtil.post(url.getAddressAndPort() + UrlConstant.RPC_URL, JSON.toJSONString(invocation));
                    RequestResult requestResult = JSON.parseObject(result, RequestResult.class);
                    if (requestResult.isSuccess()) {
                        if (method.getReturnType() == String.class) {
                            return requestResult.getData();
                        }
                        return JSON.toJavaObject((JSON) requestResult.getData(), method.getReturnType());
                    }
                    log.error("提供者返回接口错误: {}", JSON.toJSONString(requestResult));
                    // 如果返回接口错误，说明provider有问题，从本地缓存剔除，防止下次再次调用
                    LocalConsumerCache.remove(interfaceClass.getName(), url);
                    throw new RuntimeException(requestResult.getMessage());
                });
    }
}
```

github源码地址：

[https://github.com/xc-lin/frank-dubbo.git](