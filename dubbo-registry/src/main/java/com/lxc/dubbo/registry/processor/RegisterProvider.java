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

@Component
public class RegisterProvider implements BeanPostProcessor {

    @Autowired
    private Registry providerZookeeperRegistry;

    @Value("${server.port}")
    private String port;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        if (beanClass.isAnnotationPresent(FrankDubbo.class)) {
            Class<?>[] interfaces = beanClass.getInterfaces();
            Arrays.stream(interfaces).forEach(i -> {

                LocalProviderCache.register(i.getName(), beanClass, beanName);
                String hostAddress = "";
                try {
                    hostAddress = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }

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
