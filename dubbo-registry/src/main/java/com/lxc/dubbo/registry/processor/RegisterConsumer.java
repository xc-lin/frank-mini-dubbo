package com.lxc.dubbo.registry.processor;

import com.lxc.dubbo.domain.Url;
import com.lxc.dubbo.registry.Registry;
import com.lxc.dubbo.registry.annotaion.FrankDubboReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

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
        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        String hostAddress = "";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        String finalHostAddress = hostAddress;
        Arrays.stream(fields).forEach(field -> {
            if (field.isAnnotationPresent(FrankDubboReference.class)) {
                field.setAccessible(true);
                try {
                    consumerZookeeperRegistry.register(field.getType().getName(), new Url(finalHostAddress, port));
                    providerZookeeperRegistry.getUrls(field.getType().getName());
                    providerZookeeperRegistry.watchInterface(field.getType().getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return bean;
    }
}
