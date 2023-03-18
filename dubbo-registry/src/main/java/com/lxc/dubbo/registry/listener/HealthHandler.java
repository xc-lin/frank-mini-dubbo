package com.lxc.dubbo.registry.listener;


import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lxc.dubbo.domain.Url;
import com.lxc.dubbo.domain.constants.UrlConstants;
import com.lxc.dubbo.domain.enums.HealthEnum;
import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.Authenticator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class HealthHandler implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ScheduledExecutorService executor;

    @Autowired
    private CuratorFramework client;


    @SneakyThrows
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        executor.scheduleAtFixedRate(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                    List<String> interfaceNames = client.getChildren().forPath("/");
                    for (String interfaceName : interfaceNames) {
                        List<String> urls = client.getChildren().forPath("/" + interfaceName);
                        for (String stringUrl : urls) {
                            Url url = JSON.parseObject(stringUrl, Url.class);
                            String status = HttpUtil.get("127.0.0.1:8081" + UrlConstants.HEALTH_URL);
                            if (HealthEnum.HEALTH.getStatus().toString().equals(status)) {
                                System.out.println("alive");
                            } else {
                                try {
                                    client.delete().forPath(String.format("/%s/%s", interfaceName, stringUrl));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

            }
        }, 0, 5, TimeUnit.SECONDS);

        client.getChildren().usingWatcher(new CuratorWatcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println(watchedEvent);
            }
        }).forPath("/");
    }
}
