package com.lxc.dubbo.core.proxy;


import cn.hutool.http.HttpException;
import com.lxc.dubbo.core.annotaion.FrankDubboReference;
import com.lxc.dubbo.core.cache.LocalConsumerCache;
import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.domain.excetion.ApiErrCodeException;
import com.lxc.dubbo.core.loadbalance.LoadBalance;
import com.lxc.dubbo.core.util.LogUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.lxc.dubbo.core.domain.enums.ApiErrCodeExceptionEnum.NO_ALIVE_PROVIDER;

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
