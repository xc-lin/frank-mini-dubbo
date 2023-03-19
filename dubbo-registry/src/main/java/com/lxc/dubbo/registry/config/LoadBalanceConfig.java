package com.lxc.dubbo.registry.config;

import com.lxc.dubbo.registry.loadbalance.LoadBalance;
import com.lxc.dubbo.registry.loadbalance.impl.RoundRobinLoadBalance;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadBalanceConfig {

    @ConditionalOnMissingBean(LoadBalance.class)
    @Bean
    LoadBalance loadBalance(){
        return new RoundRobinLoadBalance();
    }
}
