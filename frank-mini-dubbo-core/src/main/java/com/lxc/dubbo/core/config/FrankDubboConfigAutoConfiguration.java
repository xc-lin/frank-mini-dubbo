package com.lxc.dubbo.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@ComponentScan(basePackages = {"com.lxc.dubbo"})
@PropertySource("application-frank-mini-dubbo.properties")
@Configuration
public class FrankDubboConfigAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ScheduledExecutorService threadPoolExecutor(){
        return Executors.newScheduledThreadPool(6);
    }

}
