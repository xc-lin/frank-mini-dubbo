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
