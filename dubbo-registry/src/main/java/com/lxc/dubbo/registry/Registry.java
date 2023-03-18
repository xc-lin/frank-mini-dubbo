package com.lxc.dubbo.registry;

import com.lxc.dubbo.domain.Url;
import org.apache.zookeeper.KeeperException;

import java.util.List;

public interface Registry {

    void register(String interfaceName, Url url) throws Exception;

    void logOff(String interfaceName, Url url) throws Exception;

    void getUrls(String interfaceName) throws Exception;

    void watchInterface(String interfaceName) throws Exception;

}
