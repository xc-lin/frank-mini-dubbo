package com.lxc.dubbo_consumer.controller;

import com.lxc.dubbo_consumer.service.TestHelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class Test {

    @Autowired
    private TestHelloService testHelloService;
    ScheduledExecutorService scheduledExecutorService ;
    AtomicInteger counter = new AtomicInteger();

    {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(()->{
            System.out.println("qps: " + counter.get());
            counter.set(0);
        }, 0,1, TimeUnit.SECONDS);
    }
    @GetMapping("/test")
    public void test(){
        for (int i = 0; i < 6; i++) {
            new Thread(()-> {
                while (true) {
                    try {
                        testHelloService.test();
                        counter.incrementAndGet();
                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
                    }

                }
            }).start();
        }

    }

    public static void main(String[] args) {
        Test test = new Test();
        for (int i = 0; i < 10; i++) {
            new Thread(()-> {
                while (true) {
                    test.test();
                }
            }).start();
        }
    }


    @GetMapping("/test2")
    public void test2(){
        testHelloService.testUserInfo();
    }

    @GetMapping("/test3")
    public void test3(){
        testHelloService.testVoid();
    }
}
