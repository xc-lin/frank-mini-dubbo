package com.lxc.dubbo.core.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.applicationContext = applicationContext;
    }

    public static <T> T getSpringBeanById(String beanId) {
        return (T) applicationContext.getBean(beanId);
    }

    public static <T> T getSpringBeanByType(Class<T> clazz) {
        return (T) applicationContext.getBean(clazz);
    }

    public static <T> T getSpringBeanByTypeAndId(String beanName, Class<T> clazz) {
        return (T) applicationContext.getBean(beanName, clazz);
    }

    public static ApplicationContext getContext() {
        return applicationContext;
    }
}
