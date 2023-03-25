package com.lxc.dubbo.registry.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("com.lxc.dubbo")
@Component
@Data
public class ZookeeperProperties {
    private String host = "127.0.0.1";

    private String port = "2181";

    private Integer sessionTimeoutMs = 60 * 1000;

    private Integer connectionTimeoutMs = 15 * 1000;

    private String namespace="frank/dubbo";

}
