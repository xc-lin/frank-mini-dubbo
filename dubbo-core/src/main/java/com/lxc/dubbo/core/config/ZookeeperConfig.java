package com.lxc.dubbo.core.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class ZookeeperConfig {

    @Autowired
    ZookeeperProperties zookeeperProperties;

    @Bean
    public CuratorFramework zookeeper() {
        //重试策略
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(3000, 10);

        //常用第二种方式，更直观
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(String.format("%s:%s", zookeeperProperties.getHost(), zookeeperProperties.getPort()))
                .sessionTimeoutMs(zookeeperProperties.getSessionTimeoutMs())
                .connectionTimeoutMs(zookeeperProperties.getConnectionTimeoutMs())
                .retryPolicy(retryPolicy)
                .namespace(zookeeperProperties.getNamespace())
                .build();
        //开启连接
        client.start();
        return client;
    }
}
