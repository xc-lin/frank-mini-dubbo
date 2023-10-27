package com.lxc.dubbo.core.proxy;


import com.lxc.dubbo.core.annotaion.FrankDubboReference;
import com.lxc.dubbo.core.loadbalance.LoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;

@Slf4j
public abstract class AbstractConsumerProxy implements BeanPostProcessor {

    @Autowired
    protected LoadBalance loadBalance;

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

    public abstract Object getProxy(Class interfaceClass, FrankDubboReference annotation);
}
