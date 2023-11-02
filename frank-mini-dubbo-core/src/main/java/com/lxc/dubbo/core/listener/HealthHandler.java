package com.lxc.dubbo.core.listener;


import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledExecutorService;

@Component
@Deprecated
public class HealthHandler implements ApplicationListener<WebServerInitializedEvent> {

    @Autowired
    private ScheduledExecutorService executor;

    @Autowired
    private CuratorFramework client;


    @SneakyThrows
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
//        executor.scheduleAtFixedRate(new Runnable() {
//            @SneakyThrows
//            @Override
//            public void run() {
//
//            }
//        }, 0, 2, TimeUnit.SECONDS);


    }
}
