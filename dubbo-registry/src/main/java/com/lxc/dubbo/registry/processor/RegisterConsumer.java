package com.lxc.dubbo.registry.processor;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.annotaion.FrankDubboReference;
import com.lxc.dubbo.domain.Invocation;
import com.lxc.dubbo.domain.Url;
import com.lxc.dubbo.domain.constants.UrlConstants;
import com.lxc.dubbo.registry.Registry;
import com.lxc.dubbo.registry.cache.LocalCache;
import com.lxc.dubbo.registry.loadbalance.LoadBalance;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

@Component
public class RegisterConsumer implements BeanPostProcessor {

    @Autowired
    private Registry registry;

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
                    field.set(bean, getProxy(field.getType()));
                    registry.getUrls(field.getType().getName());
                    registry.watchInterface(field.getType().getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return bean;
    }

    private Object getProxy(Class interfaceClass) {
        return Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Invocation invocation = new Invocation(interfaceClass.getName(), method.getName(), args, method.getParameterTypes());
                List<Url> urls = LocalCache.get(interfaceClass.getName());
                Url url = loadBalance.getUrl(urls);
                String result = HttpUtil.post(url.getAddressAndPort() + UrlConstants.RPC_URL, JSON.toJSONString(invocation));
                if (method.getReturnType() == String.class) {
                    return result;
                }
                return JSON.parseObject(result, method.getReturnType());
            }
        });
    }
}
