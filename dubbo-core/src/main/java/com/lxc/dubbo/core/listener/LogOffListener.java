package com.lxc.dubbo.core.listener;

import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.util.NetUtil;
import com.lxc.dubbo.core.cache.LocalConsumerCache;
import com.lxc.dubbo.core.cache.LocalProviderCache;
import com.lxc.dubbo.core.register.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LogOffListener implements ApplicationListener<ContextStoppedEvent> {

    @Autowired
    private Registry providerZookeeperRegistry;

    @Autowired
    private Registry consumerZookeeperRegistry;

    @Value("${server.port}")
    private String port;

    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {
        Set<String> allInterfaces = LocalProviderCache.getAllInterfaces();
        String hostAddress = NetUtil.getIpAddress();

        for (String inter : allInterfaces) {
            try {
                providerZookeeperRegistry.logOff(inter, new Url(hostAddress, port));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        Set<String> allConsumerInterfaces = LocalConsumerCache.getAllInterfaces();

        for (String inter : allConsumerInterfaces) {
            try {
                consumerZookeeperRegistry.logOff(inter, new Url(hostAddress, port));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
