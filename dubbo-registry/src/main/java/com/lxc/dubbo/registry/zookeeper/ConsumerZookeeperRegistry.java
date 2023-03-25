package com.lxc.dubbo.registry.zookeeper;

import com.lxc.dubbo.domain.constants.RegistryConstant;
import org.springframework.stereotype.Component;

@Component
public class ConsumerZookeeperRegistry extends AbstractZookeeperRegistry{


    @Override
    public String getPrefix() {
        return RegistryConstant.CONSUMER;
    }
}
