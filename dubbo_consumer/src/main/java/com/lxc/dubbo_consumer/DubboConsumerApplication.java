package com.lxc.dubbo_consumer;

import com.lxc.dubbo_consumer.service.TestHelloService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class DubboConsumerApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DubboConsumerApplication.class, args);
        TestHelloService bean = applicationContext.getBean(TestHelloService.class);
        bean.test();
    }

}
