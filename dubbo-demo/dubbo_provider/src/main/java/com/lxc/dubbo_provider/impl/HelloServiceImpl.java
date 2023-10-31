package com.lxc.dubbo_provider.impl;

import com.lxc.domain.User;
import com.lxc.dubbo.core.annotaion.FrankDubbo;
import com.lxc.interfaces.HelloService;

@FrankDubbo(flowLimit = -1)
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello() {
        return "hello frank dubbo !!!";
    }

    @Override
    public User getUserInfo() {
        User user = new User();
        user.setPassword("1111");
        user.setUsername("1111");
        return user;
    }

    @Override
    public User testVoid() {
        System.out.println("testVoid");
        User user = new User();
        user.setPassword("1111");
        user.setUsername("1111");
        throw new RuntimeException("123");

    }
}
