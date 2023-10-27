package com.lxc.dubbo.core.annotaion;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface FrankDubboReference {
    long timeout() default 5000L;
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}