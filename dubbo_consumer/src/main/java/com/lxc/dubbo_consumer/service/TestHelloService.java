package com.lxc.dubbo_consumer.service;

import com.alibaba.fastjson.JSON;
import com.lxc.domain.User;
import com.lxc.dubbo.registry.annotaion.FrankDubboReference;
import com.lxc.interfaces.HelloService;
import org.springframework.stereotype.Service;

@Service
public class TestHelloService {

    @FrankDubboReference
    HelloService helloService;

    public void test() {
        String s = helloService.sayHello();
        System.out.println(s);
    }

    public void testUserInfo(){
        User userInfo = helloService.getUserInfo();
        System.out.println(JSON.toJSONString(userInfo));
    }

    public void testVoid(){
        User user = helloService.testVoid();
        System.out.println(JSON.toJSONString(user));
    }
}
