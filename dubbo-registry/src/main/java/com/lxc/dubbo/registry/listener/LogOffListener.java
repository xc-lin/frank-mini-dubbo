package com.lxc.dubbo.registry.listener;

import com.lxc.dubbo.domain.Url;
import com.lxc.dubbo.registry.cache.LocalProviderCache;
import com.lxc.dubbo.registry.Registry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

@Component
public class LogOffListener implements ApplicationListener<ContextStoppedEvent> {

    @Autowired
    private Registry registry;

    @Value("${server.port}")
    private String port;

    @Override
    public void onApplicationEvent(ContextStoppedEvent event) {
        Set<String> allInterfaces = LocalProviderCache.getAllInterfaces();
        String hostAddress = "";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }


        for (String inter : allInterfaces) {
            try {
                registry.logOff(inter, new Url(hostAddress, port));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
