# Frank mini dubbo

# 手写一个mini dubbo，彻底搞懂dubbo的基本原理

1. 首先我认为dubbo帮助我们解决了，微服务下应用与应用之间调用的复杂问题。
2. 如果没有dubbo，微服务这种架构还能实现吗？答案是肯定的，我们可以用http的形式，在每一个需要调用外部接口的地方，都采用http调用，并在provider中将接口通过http的形式暴露给外部系统。这样也能实现微服务之间的调用
3. 但是随之而来的就是大量的重复代码，大量的http调用，十分麻烦。
4. 因此dubbo应运而生。

---

## 我自己手写的dubbo是一套简单基于http/netty和springboot的rpc远程方法调用的开箱即用的框架，并且兼容了hessian和json两种序列化方式，并使用zookeeper作为注册中心

1. 能实现负载均衡，并且可以支持自己实现负载均衡算法，目前支持轮询和随机
2. 能实现服务的自动注册，以及下线
3. 能实现消费者像调用**本地方法**一样调用**远程方法**
4. 实现了提供者端支持通过注解标识**限流**
5. 实现了**令牌**桶机制的单机**限流工具**
6. consumer端支持设置**超时时间**

---

下面向大家介绍各个模块的作用

1. dubbo-core是核心包，实现了rpc远程调用
2. dubbo-demo为demo

默认的provider:  zookeeper路径为 /frank/dubbo/provider/netty/{interfaceName}/{url}

默认的consumer:  zookeeper路径为 /frank/dubbo/consumer/netty/{interfaceName}/{url}

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

### 注册provider

这一步总体来说就是，基于spring bean的后置处理器来将当前provider指定的接口暴露出去

判断当前类是否有FrankDubbo注解，如果有则

```java
/**
 * 向注册中心注册provider
 */
@Component
public class RegisterProvider implements BeanPostProcessor {

    @Autowired
    private Registry providerZookeeperRegistry;

    @Value("${frank.dubbo.netty.port}")
    private String nettyPort;

    @Value("${server.port}")
    private String httpPort;

    @Value("${protocol}")
    protected String protocol;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        String hostAddress = NetUtil.getIpAddress();
        // 如果是根据协议获取暴露的端口
        String port = Objects.equals(protocol, ProtocolConstants.NETTY) ? nettyPort : httpPort;
        if (beanClass.isAnnotationPresent(FrankDubbo.class)) {
            Class<?>[] interfaces = beanClass.getInterfaces();
            Arrays.stream(interfaces).forEach(i -> {
                LocalProviderCache.register(i.getName(), beanClass, beanName, beanClass.getAnnotation(FrankDubbo.class));
                try {
                    providerZookeeperRegistry.register(i.getName(), new Url(hostAddress, port));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return bean;
    }
}

    @Override
    public void register(String interfaceName, Url url) {
        try {
            if (client.checkExists().forPath("/" + getPrefix() + "/" + interfaceName) == null) {
                // 创建永久节点
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + getPrefix() + "/" + interfaceName);
            }
            // 创建临时节点，当机器下线，zookeeper则自动删除当前节点
            String nodePath = client.create().withMode(CreateMode.EPHEMERAL).forPath(String.format("/" + getPrefix() + "/%s/%s", interfaceName, JSON.toJSONString(url)));
            LogUtil.info("frank mini dubbo register service: {} on dubbo node: {}", interfaceName, nodePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getPrefix() {
        return RegistryConstant.PROVIDER + "/" + protocol;
    }

```

### provider的请求分发/反射调用

1. **http协议**

```java
/**
 * provider的请求分发
 */
@RestController
@Slf4j
public class ProviderMethodInvocation {
    @PostMapping(UrlConstant.RPC_URL)
    public Object dubbo(@RequestBody Invocation invocation) throws Exception {
        Object result = MethodInvocation.providerMethodInvocation(invocation);
        return result;
    }
}
```

2. **netty协议**

   标记@ConditionalOnProperty注解仅在协议为netty的情况下才生效，监听容器刷新完成事件，并启动netty服务器

```java
@Component
@ConditionalOnProperty(value = "protocol", havingValue = ProtocolConstants.NETTY)
public class NettyServer implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${frank.dubbo.netty.port}")
    private String nettyPort;

    public static void startServer(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    // 解决粘包问题
                                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 12, 4, 0, 0))
                                    // 解析自己的协议
                                    .addLast(new FrankMiniDubboCodec())
                                    // Invocation序列化/反序列化
                                    .addLast(new FrankMiniDubboInvocationHandler())
                                    // Response序列化/反序列化
                                    .addLast(new FrankMiniDubboResponseSerializeHandler())
                                    // 反射调用
                                    .addLast(new NettyServerHandler());
                        }
                    });

            String hostAddress = NetUtil.getIpAddress();
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            LogUtil.info("当前服务器netty已启动, {}:{}", hostAddress, port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
      	// 启动netty服务器
        NettyServer.startServer(Integer.parseInt(nettyPort));
        LogUtil.info("NettyServer已启动!");
    }
```

当通道有读取事件时，根据invocation信息，反射调用具体接口实现，构造返回结果，并写消息返回consumer

```
public class NettyServerHandler extends ChannelDuplexHandler {


    //当通道就绪就会触发该方法
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        LogUtil.info("provider: {} 成功连接, consumer: {}", channel.localAddress(), channel.remoteAddress());
    }

    //当通道有读取事件时，会触发
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Invocation invocation = (Invocation) msg;
        Object o = null;
        RequestResult requestResult = null;
        try {
            o = MethodInvocation.providerMethodInvocation(invocation);
            requestResult = RequestResult.buildSuccess(o);
        } catch (Exception e) {
            requestResult = RequestResult.buildFailure(e);
        }
        requestResult.setUuid(invocation.getUuid());
        String serializeTypeInSystem = ApplicationContextUtil.getContext().getEnvironment().getProperty("serializeType");
        int serializeType = SerializeTypeEnum.getByName(serializeTypeInSystem).getCode();
        ctx.writeAndFlush(new FrankMiniDubboResultMessage(0, 0, serializeType, requestResult));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```





---

## 消费者

### 注册consumer

向注册中心注册消费者，这一步对于整个系统的调用没有实质作用，但是可以便于维护。可以看到，每个接口的实际调用者。

最重要的是，获取所有的provider 并与他们建立连接。

监听当前接口的注册中心变化，根据注册中心变化，实时更新本地缓存，新建连接或者关闭连接。

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
        // 获取当前bean的所有属性
        Field[] fields = beanClass.getDeclaredFields();
        // 获取当前服务器的ip地址
        String finalHostAddress = NetUtil.getIpAddress();
        // 遍历每个对象，将其注入到本地缓存和注册中心中，并获取provider，以及监听当前接口
        Arrays.stream(fields).forEach(field -> {
            // 判断对象上是否有FrankDubboReference注解
            if (field.isAnnotationPresent(FrankDubboReference.class)) {
                field.setAccessible(true);
                try {
                    // consumer注册到zookeeper上
                    consumerZookeeperRegistry.register(field.getType().getName(), new Url(finalHostAddress, port));
                    // 获取当前接口所有的provider，并将它存储到本地缓存中，interfaceName，List<Url>
                    // consumer启动netty客户端与provider连接
                    providerZookeeperRegistry.getUrls(field.getType().getName());
                    // 监听当前接口的注册中心变化，根据注册中心变化，实时更新本地缓存，新建连接或者关闭连接
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

### 动态代理(http和netty协议)

**动态代理基类**，完成动态代理的核心通用逻辑，首先再次依赖于spring的bean的后置处理器，判断每个bean内部的属性是否有FrankDubboReference注解，如果有则将此接口设置为动态代理的类。

getProxy：首先获取当前接口的所有provider，再根据spring容器中的负载均衡器来获取目标的provider，然后调用抽象方法rpc，netty和http协议唯一的不同就在此，如何实现远程调用。

```java
/**
 * consumer 动态代理基类
 */
public abstract class AbstractConsumerProxy implements BeanPostProcessor {

    @Autowired
    protected LoadBalance loadBalance;

    @Value("${serializeType:json}")
    protected String serializeType;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            // 判断属性上是否有FrankDubboReference属性
            if (field.isAnnotationPresent(FrankDubboReference.class)) {
                field.setAccessible(true);
                try {
                    // 将属性设置为动态代理的类
                    field.set(bean, getProxy(field.getType(), field.getAnnotation(FrankDubboReference.class)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return bean;
    }

    /**
     * 获取接口的动态代理类
     * @param interfaceClass
     * @param frankDubboReference
     * @return
     */
    private Object getProxy(Class interfaceClass, FrankDubboReference frankDubboReference) {
        return Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, (proxy, method, args) -> {
            // 构建调用对象，请求到provider
            Invocation invocation = new Invocation(interfaceClass.getName(), method.getName(), args, method.getParameterTypes(), UUID.randomUUID().toString());
            // 获取当前接口的所有provider
            List<Url> urls = LocalConsumerCache.get(interfaceClass.getName());
            if (CollectionUtils.isEmpty(urls)) {
                LogUtil.error("当前接口: {}，没有存活的提供者: {}", interfaceClass.getName(), urls);
                throw new ApiErrCodeException(NO_ALIVE_PROVIDER);
            }
            LogUtil.debug("当前接口: {}，存活的提供者: {}", interfaceClass.getName(), urls.toString());
            // 根绝负载均衡算法获取目标provider
            Url url = loadBalance.getUrl(urls);
            LogUtil.debug("当前接口: {}，选择: {}", interfaceClass.getName(), url);
            try {
                // 远程调用
                return rpcExecute(method, invocation, url, frankDubboReference);
            } catch (HttpException | TimeoutException exception) {
                throw new TimeoutException(String.format("failed to call %s on remote server %s, Timeout: %s", invocation.getInterfaceName(), url.getAddressAndPort(), frankDubboReference.timeUnit().toMillis(frankDubboReference.timeout())));
            }
        });
    }

    public abstract Object rpcExecute(Method method, Invocation invocation, Url url, FrankDubboReference frankDubboReference) throws TimeoutException, ExecutionException, InterruptedException;
}
```

#### http协议

发送http 请求，等待结果或超时

```java
@Component
@Slf4j
@ConditionalOnProperty(value = "protocol", havingValue = ProtocolConstants.HTTP)
public class HttpConsumerProxy extends AbstractConsumerProxy {

    @Override
    public Object rpcExecute(Method method, Invocation invocation, Url url, FrankDubboReference frankDubboReference) {
        // 获取注解上的超时时间
        long timeoutMillis = frankDubboReference.timeUnit().toMillis(frankDubboReference.timeout());
        // 发送http请求
        String result = HttpUtil.post(url.getAddressAndPort() + UrlConstant.RPC_URL, JSON.toJSONString(invocation), (int) timeoutMillis);
        // 解析反序列化结果
        RequestResult requestResult = JSON.parseObject(result, RequestResult.class);
        if (requestResult.isSuccess()) {
            if (method.getReturnType() == String.class) {
                return requestResult.getData();
            }
            return JSON.toJavaObject((JSON) requestResult.getData(), method.getReturnType());
        }
//        LogUtil.error("提供者返回接口错误: {}", JSON.toJSONString(requestResult));
        throw new RuntimeException(requestResult.getMessage());

    }
}
```

#### netty

首先根据当前的url，获取netty连接，发送请求，并阻塞，直到请求返回结果,或超时(这里的超时是用futrue.get来实现的超时)

```java
@Component
@ConditionalOnProperty(value = "protocol", havingValue = ProtocolConstants.NETTY)
public class NettyConsumerProxy extends AbstractConsumerProxy {

    public Object rpcExecute(Method method, Invocation invocation, Url url, FrankDubboReference frankDubboReference) throws TimeoutException, ExecutionException, InterruptedException {
        // 获取当前url对应的netty客户端，其中包含了通道，和连接
        NettyClient nettyClient = LocalConsumerCache.get(url);
        // 发送请求，并阻塞，直到请求返回结果,或超时(这里的超时是用futrue.get来实现的超时)
        RequestResult requestResult = nettyClient.send(invocation, frankDubboReference.timeout(), frankDubboReference.timeUnit());
        if (requestResult.isSuccess()) {
            if (method.getReturnType() == String.class) {
                return requestResult.getData();
            }

            // 如果序列化方式是json，还需要将返回结果反序列化一次
            if (Objects.equals(serializeType, SerializeTypeEnum.JSON.getName())) {
                return JSON.toJavaObject((JSON) requestResult.getData(), method.getReturnType());
            }
            return requestResult.getData();
        }
//                    LogUtil.error("提供者返回接口错误: {}", JSON.toJSONString(requestResult));
        throw new RuntimeException(requestResult.getMessage());
    }
}
```

## 总结

到这里其实我手写的mini dubbo的核心逻辑就已经讲解完了，我始终认为一个rpc协议，只有四个核心技术：

1. 动态代理
2. 网络请求协议，http，netty(tcp)传输层，需要自己解决很多问题，比如粘包拆包，协议头的定义
3. 序列化/反序列化
4. 反射

我们不管看dubbo还是open Feign都是执着于这四点

---

## netty实现具体代码逻辑

前面看到了netty客户端和服务端是如何启动的，接下来我将继续具体讲讲pipeline中各个ChannelHandler的逻辑

1. 第一个为内部协议解析的codec本质上也是一个channelHandler，

   **协议：4字节 魔数| 1字节版本号 ｜1字节序列化方式 ｜ 4字节请求序号 ｜ 2字节，无效内容补齐12字节｜4字节内容长度｜实际内容**

```java
/** 
 * 内部协议的解析
 */
public class FrankMiniDubboCodec extends ByteToMessageCodec<FrankMiniDubboProtocol> {

    private final int magicNum = 19980120;

    /**
     * 读消息
     * 解析协议
     * @param channelHandlerContext
     * @param byteBuf
     * @param list
     * @throws Exception
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 4字节，魔数，用来判断数据包是否有效
        int magic = byteBuf.readInt();
        if (magic != magicNum) {
            return;
        }
        // 1字节 版本号，暂时无用
        int versionNum = byteBuf.readByte();
        // 1字节 序列化方式
        int serializeType = byteBuf.readByte();
        // 4字节 请求序号，暂时无用，在具体数据中有个uuid使用它当作请求唯一标识，后面将会优化
        int sequenceId = byteBuf.readInt();
        // 2字节，字节补齐到2的n次方 读取多余的无用数据
        byteBuf.readByte();
        byteBuf.readByte();
        // 4字节，内容长度
        int length = byteBuf.readInt();
        byte[] contentBytes = new byte[length];
        // 实际内容
        byteBuf.readBytes(contentBytes);
        FrankMiniDubboProtocol frankMiniDubboProtocol = new FrankMiniDubboProtocol(sequenceId, length, serializeType, contentBytes);
        list.add(frankMiniDubboProtocol);
    }

    /**
     * 编码协议
     * @param channelHandlerContext
     * @param protocol
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, FrankMiniDubboProtocol protocol, ByteBuf byteBuf) throws Exception {
        // 4字节 写入4字节的魔数，判断数据包有效性
        byteBuf.writeInt(magicNum);
        // 1字节 版本号
        byteBuf.writeByte(1);
        // 1字节 序列化方式
        byteBuf.writeByte(protocol.getSerializeType());
        // 4字节 请求序号，暂时无用
        byteBuf.writeInt(protocol.getSequenceId());
        // 2字节 占用两字节
        byteBuf.writeByte(0xff);
        byteBuf.writeByte(0xff);
        // 4字节 内容长度
        byteBuf.writeInt(protocol.getLength());
        byteBuf.writeBytes(protocol.getContentBytes());
    }
}
```

2、序列化 反序列化

此handler主要用来序列化/反序列化请求消息，以及结果返回消息

通过byte字节数组，转换为具体的对象，目前一共有两种实现json，hessian，还可以增加msgpack

```java
public abstract class AbstractFrankMiniDubboSerializeHandler<T> extends ChannelDuplexHandler {
		/**
     * 序列化反序列化处理的类，给json反序列化使用
     */
    Class<T> clazz;

    public AbstractFrankMiniDubboSerializeHandler(Class clazz) {
        this.clazz = clazz;
    }

    /**
     * 读消息
     * 反序列化消息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 判断是否由当前handler处理
        if (!(msg instanceof FrankMiniDubboProtocol)) {
            ctx.fireChannelRead(msg);
            return;
        }
        FrankMiniDubboProtocol frankMiniDubboProtocol = (FrankMiniDubboProtocol) msg;
        // 获取序列化方式
        int serializeType = frankMiniDubboProtocol.getSerializeType();
        // 请求序号
        int sequenceId = frankMiniDubboProtocol.getSequenceId();
        // 内容长度
        int length = frankMiniDubboProtocol.getLength();
        byte[] contentBytes = frankMiniDubboProtocol.getContentBytes();
        T data;
        // 根据序列化方式，反序列化
        if (serializeType == SerializeTypeEnum.HESSIAN.getCode()) {
            ByteArrayInputStream in = new ByteArrayInputStream(contentBytes);
            Hessian2Input input = new Hessian2Input(in);
            data = (T) input.readObject();
        } else {
            String jsonString = new String(contentBytes, CharsetUtil.UTF_8);
            data = JSON.parseObject(jsonString, clazz);
        }
        // 将反序列化后的消息传到下一个handler中
        ctx.fireChannelRead(data);

    }

    /**
     * 写消息
     * 序列化消息
     * @param ctx     the {@link ChannelHandlerContext} for which the write operation is made
     * @param msg     the message to write
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // 判断是否由当前handler处理
        if (!(msg instanceof FrankMiniDubboBaseMessage)) {
            ctx.write(msg, promise);
            return;
        }
        FrankMiniDubboBaseMessage frankMiniDubboBaseMessage = (FrankMiniDubboBaseMessage) msg;
        if (Objects.isNull(frankMiniDubboBaseMessage.getData()) || !Objects.equals(frankMiniDubboBaseMessage.getData().getClass(), clazz)) {
            ctx.write(msg, promise);
            return;
        }
        // 获取序列化方式
        int serializeType = frankMiniDubboBaseMessage.getSerializeType();
        Object data = frankMiniDubboBaseMessage.getData();

        byte[] contentBytes;
        // 根据序列化协议，动态选择,并序列化消息
        if (serializeType == SerializeTypeEnum.HESSIAN.getCode()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Hessian2Output output = new Hessian2Output(os);
            output.writeObject(data);
            output.close();
            contentBytes = os.toByteArray();
        } else {
            contentBytes = JSON.toJSONBytes(data);
        }

        // 传到下一个handler中
        ctx.write(new FrankMiniDubboProtocol(frankMiniDubboBaseMessage.getSequenceId(), contentBytes.length, serializeType, contentBytes));
    }
}
```

大家还记得上面说的，使用netty(tcp)作为网络协议，需要解决粘包拆包的问题，但是上面代码并没有哪里实现了粘包拆包

其实通过自定义协议中，定义了内容长度并加上LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 12, 4, 0, 0)这一个channelHandler就可以实现粘包拆包的处理

1） maxFrameLength - 发送的数据包最大长度；

（2） lengthFieldOffset - 长度域偏移量，指的是长度域位于整个数据包字节数组中的下标；

（3） lengthFieldLength - 长度域的自己的字节数长度。

（4） lengthAdjustment – 长度域的偏移量矫正。 如果长度域的值，除了包含有效数据域的长度外，还包含了其他域（如长度域自身）长度，那么，就需要进行矫正。矫正的值为：包长 - 长度域的值 – 长度域偏移 – 长度域长。

（5） initialBytesToStrip – 丢弃的起始字节数。丢弃处于有效数据前面的字节数量。比如前面有4个节点的长度域，则它的值为4。



# 结语

到这里frank-mini-dubbo的核心逻辑讲解就结束了

大家有什么问题可以联系xianchaolin@126.com

下面有github地址，希望大家三连(点赞，收藏，评论)😄

**[csdn地址](https://blog.csdn.net/weixin_42293662/article/details/129779170)**




