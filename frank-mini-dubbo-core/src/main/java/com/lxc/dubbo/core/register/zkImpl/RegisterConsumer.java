package com.lxc.dubbo.core.register.zkImpl;

import com.lxc.dubbo.core.register.Registry;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.util.NetUtil;
import com.lxc.dubbo.core.annotaion.FrankDubboReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * 向注册中心注册消费者，这一步对于整个系统的调用没有实质作用，但是可以便于维护。可以看到，每个接口的实际调用者，
 * 最重要的是，获取所有的provider 并与他们建立连接
 * 监听当前接口的注册中心变化，根据注册中心变化，实时更新本地缓存，新建连接或者关闭连接
 */
@Component
public class RegisterConsumer implements BeanPostProcessor {

    @Autowired
    private Registry providerZookeeperRegistry;

    @Autowired
    private Registry consumerZookeeperRegistry;

    @Value("${server.port}")
    private String port;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 获取当前bean的class
        Class<?> beanClass = bean.getClass();
        // 获取当前bean的所有属性
        Field[] fields = beanClass.getDeclaredFields();
        // 获取当前服务器的ip地址
        String finalHostAddress = NetUtil.getIpAddress();
        // 遍历每个对象，将其注入到本地缓存和注册中心中，并获取provider，以及监听当前接口
        Arrays.stream(fields).forEach(field -> {
            // 判断对象上是否有FrankDubboReference注解
            if (field.isAnnotationPresent(FrankDubboReference.class)) {
                field.setAccessible(true);
                try {
                    // consumer注册到zookeeper上
                    consumerZookeeperRegistry.register(field.getType().getName(), new Url(finalHostAddress, port));
                    // 获取当前接口所有的provider，并将它存储到本地缓存中，interfaceName，List<Url>
                    // consumer启动netty客户端与provider连接
                    providerZookeeperRegistry.getUrls(field.getType().getName());
                    // 监听当前接口的注册中心变化，根据注册中心变化，实时更新本地缓存，新建连接或者关闭连接
                    providerZookeeperRegistry.watchInterface(field.getType().getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return bean;
    }
}
