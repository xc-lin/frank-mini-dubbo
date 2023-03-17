package com.lxc.dubbo_provider;

import com.lxc.dubbo_provider.impl.HelloServiceImpl;
import com.lxc.interfaces.HelloService;
import com.lxc.register.LocalCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DubboProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DubboProviderApplication.class, args);
        LocalCache.set(HelloService.class.getName(), HelloServiceImpl.class);
    }

}
