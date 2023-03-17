package com.lxc.dubbo.processor;

import com.lxc.dubbo.annotaion.FrankDubbo;
import com.lxc.dubbo.register.LocalCache;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class post implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if (beanClass.isAnnotationPresent(FrankDubbo.class)){
            Class<?>[] interfaces = beanClass.getInterfaces();
            Arrays.stream(interfaces).forEach(i -> LocalCache.register(i.getName(), beanClass));
        }
        return bean;
    }
}
