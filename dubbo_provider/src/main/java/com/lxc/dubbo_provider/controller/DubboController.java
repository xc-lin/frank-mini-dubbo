package com.lxc.dubbo_provider.controller;


import com.alibaba.fastjson.JSON;
import com.lxc.domain.Invocation;
import com.lxc.register.LocalCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class DubboController {

    @RequestMapping("/dubbo")
    public Object dubbo(@RequestBody Invocation invocation) throws Exception {
        Class clazz = LocalCache.get(invocation.getInterfaceName());
        log.info("执行{}.{}",invocation.getInterfaceName(),invocation.getMethodName());
        Object result = clazz.getMethod(invocation.getMethodName(), invocation.getParamTypes()).invoke(clazz.newInstance(), invocation.getParams());
        log.info("result:{}", JSON.toJSONString(result));
        return result;
    }
}
