package com.lxc.dubbo.registry;

import com.lxc.dubbo.domain.Url;
import org.apache.zookeeper.KeeperException;

public interface Registry {

    void register(String interfaceName, Url url) throws Exception;

    void logOff(String interfaceName, Url url) throws Exception;

}
