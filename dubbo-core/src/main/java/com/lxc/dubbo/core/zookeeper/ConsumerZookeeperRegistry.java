package com.lxc.dubbo.core.zookeeper;

import com.lxc.dubbo.core.domain.constants.RegistryConstant;
import org.springframework.stereotype.Component;

@Component
public class ConsumerZookeeperRegistry extends AbstractZookeeperRegistry{


    @Override
    public String getPrefix() {
        return RegistryConstant.CONSUMER;
    }
}
