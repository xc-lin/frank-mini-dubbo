package com.lxc.dubbo.registry;


import cn.hutool.http.HttpUtil;
import com.lxc.dubbo.domain.constants.UrlConstants;
import com.lxc.dubbo.domain.enums.HealthEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class healthHandler implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private ScheduledExecutorService executor;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                String s = HttpUtil.get("127.0.0.1:8081" + UrlConstants.HEALTH_URL);
                if (s.equals(HealthEnum.HEALTH.getStatus().toString())){
                    System.out.println("alive");
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
