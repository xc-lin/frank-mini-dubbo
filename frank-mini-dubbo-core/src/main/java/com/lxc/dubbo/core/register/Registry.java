package com.lxc.dubbo.core.register;

import com.lxc.dubbo.core.domain.Url;

public interface Registry {

    void register(String interfaceName, Url url) throws Exception;

    void logOff(String interfaceName, Url url) throws Exception;

    void getUrls(String interfaceName) throws Exception;

    void watchInterface(String interfaceName) throws Exception;

    String getPrefix();
}
