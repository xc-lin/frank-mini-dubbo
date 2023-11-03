package com.lxc.dubbo.core.proxy;


import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.annotaion.FrankDubboReference;
import com.lxc.dubbo.core.cache.LocalConsumerCache;
import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.RequestResult;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.domain.constants.UrlConstant;
import com.lxc.dubbo.core.domain.excetion.ApiErrCodeException;
import com.lxc.dubbo.core.loadbalance.LoadBalance;
import com.lxc.dubbo.core.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.lxc.dubbo.core.domain.enums.ApiErrCodeExceptionEnum.NO_ALIVE_PROVIDER;

@Slf4j
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
            if (field.isAnnotationPresent(FrankDubboReference.class)) {
                field.setAccessible(true);
                try {
                    field.set(bean, getProxy(field.getType(), field.getAnnotation(FrankDubboReference.class)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return bean;
    }

    public Object getProxy(Class interfaceClass, FrankDubboReference frankDubboReference) {
        return Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, (proxy, method, args) -> {
            Invocation invocation = new Invocation(interfaceClass.getName(), method.getName(), args, method.getParameterTypes(), UUID.randomUUID().toString());
            List<Url> urls = LocalConsumerCache.get(interfaceClass.getName());
            if (CollectionUtils.isEmpty(urls)) {
                LogUtil.error("当前接口: {}，没有存活的提供者: {}", interfaceClass.getName(), urls);
                throw new ApiErrCodeException(NO_ALIVE_PROVIDER);
            }
            LogUtil.debug("当前接口: {}，存活的提供者: {}", interfaceClass.getName(), urls.toString());
            Url url = loadBalance.getUrl(urls);
            LogUtil.debug("当前接口: {}，选择: {}", interfaceClass.getName(), url);
            try {
                return rpcExecute(method, invocation, url, frankDubboReference);
            } catch (HttpException | TimeoutException exception) {
                throw new TimeoutException(String.format("failed to call %s on remote server %s, Timeout: %s", invocation.getInterfaceName(), url.getAddressAndPort(), frankDubboReference.timeUnit().toMillis(frankDubboReference.timeout())));
            }
        });
    }

    public abstract Object rpcExecute(Method method, Invocation invocation, Url url, FrankDubboReference frankDubboReference) throws TimeoutException, ExecutionException, InterruptedException;
}
