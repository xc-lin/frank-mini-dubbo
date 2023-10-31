package com.lxc.dubbo.core.register.zkImpl;

import com.lxc.dubbo.core.register.Registry;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.util.NetUtil;
import com.lxc.dubbo.core.annotaion.FrankDubbo;
import com.lxc.dubbo.core.cache.LocalProviderCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

@Component
public class RegisterProvider implements BeanPostProcessor {

    @Autowired
    private Registry providerZookeeperRegistry;

    @Value("${frank.dubbo.netty.port}")
    private String nettyPort;

    @Value("${server.port}")
    private String httpPort;

    @Value("${protocol}")
    protected String protocol;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        if (beanClass.isAnnotationPresent(FrankDubbo.class)) {
            Class<?>[] interfaces = beanClass.getInterfaces();
            Arrays.stream(interfaces).forEach(i -> {

                LocalProviderCache.register(i.getName(), beanClass, beanName, beanClass.getAnnotation(FrankDubbo.class));
                String hostAddress = NetUtil.getIpAddress();
                NetUtil.getIpAddress();

                try {
                    String port = Objects.equals(protocol, "netty") ? nettyPort : httpPort;
                    providerZookeeperRegistry.register(i.getName(), new Url(hostAddress, port));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return bean;
    }
}
