package com.lxc.dubbo.core.config;

import com.lxc.dubbo.core.loadbalance.LoadBalance;
import com.lxc.dubbo.core.loadbalance.impl.RoundRobinLoadBalance;
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
