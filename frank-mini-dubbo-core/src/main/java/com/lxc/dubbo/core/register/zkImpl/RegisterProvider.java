package com.lxc.dubbo.core.register.zkImpl;

import com.lxc.dubbo.core.domain.enums.ProtocolConstants;
import com.lxc.dubbo.core.protocol.netty.NettyServer;
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
        String hostAddress = NetUtil.getIpAddress();
        // 如果是根据协议获取暴露的端口
        String port = Objects.equals(protocol, ProtocolConstants.NETTY) ? nettyPort : httpPort;
        if (beanClass.isAnnotationPresent(FrankDubbo.class)) {
            Class<?>[] interfaces = beanClass.getInterfaces();
            Arrays.stream(interfaces).forEach(i -> {
                LocalProviderCache.register(i.getName(), beanClass, beanName, beanClass.getAnnotation(FrankDubbo.class));
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
