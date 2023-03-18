package com.lxc.dubbo.config;

import cn.hutool.core.thread.RejectPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ScheduledExecutorService threadPoolExecutor(){
        return Executors.newScheduledThreadPool(6);
    }
}
