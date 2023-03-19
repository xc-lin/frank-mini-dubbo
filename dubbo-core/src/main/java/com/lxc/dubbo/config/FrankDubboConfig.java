package com.lxc.dubbo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = {"com.lxc.dubbo"})
@Configuration
public class FrankDubboConfig {
}
