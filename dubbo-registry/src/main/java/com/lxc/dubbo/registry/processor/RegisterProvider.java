package com.lxc.dubbo.registry.processor;

import com.lxc.dubbo.domain.Url;
import com.lxc.dubbo.registry.annotaion.FrankDubbo;
import com.lxc.dubbo.registry.cache.LocalProviderCache;
import com.lxc.dubbo.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

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
