package com.lxc.dubbo_consumer.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.domain.Invocation;
import com.lxc.interfaces.HelloService;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class TestHelloService {

    public void test() {
        Invocation invocation = new Invocation();
        invocation.setInterfaceName(HelloService.class.getName());
        invocation.setMethodName("sayHello");
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("invocation", JSON.toJSONString(invocation));
        String result = HttpUtil.post("127.0.0.1:8081/dubbo", JSON.toJSONString(invocation));
        System.out.println(result);
    }
}
