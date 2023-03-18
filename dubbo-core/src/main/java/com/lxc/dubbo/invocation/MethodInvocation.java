package com.lxc.dubbo.invocation;

import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.domain.Invocation;
import com.lxc.dubbo.register.LocalCache;
import com.lxc.dubbo.util.ApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class MethodInvocation {

    @PostMapping("/frankDubbo1998-01-20")
    public Object dubbo(@RequestBody Invocation invocation) throws Exception {
        Class clazz = LocalCache.get(invocation.getInterfaceName());
        log.info("执行{}.{}",invocation.getInterfaceName(),invocation.getMethodName());
        Object result = clazz.getMethod(invocation.getMethodName(), invocation.getParamTypes()).invoke(ApplicationContextUtil.getSpringBeanByType(clazz), invocation.getParams());
        log.info("result:{}", JSON.toJSONString(result));
        return result;
    }
}

