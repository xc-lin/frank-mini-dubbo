package com.lxc.dubbo_provider.impl;

import com.lxc.dubbo.annotaion.FrankDubbo;
import com.lxc.interfaces.HelloService;
@FrankDubbo
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello() {
        return "hello frank dubbo !!!";
    }
}
