# Frank mini dubbo

## frank mini dubbo 是一套简单基于http或者netty和springboot的rpc远程方法调用的**开箱即用**的基于springBoot的框架，并使用zookeeper作为注册中心
## 可参考demo，具体如何使用
1. 能实现负载均衡
2. 能实现服务的自动注册，以及下线
3. 能实现消费者像调用**本地方法**一样调用**远程方法**
4. 实现了提供者端支持通过注解标识**限流**
5. 实现了**令牌**桶机制的单机**限流工具**

---

下面向大家介绍各个模块的作用

1. dubbo-core是核心包，实现了rpc远程调用
4. dubbo-demo为demo

默认的provider:  zookeeper路径为 /frank/dubbo/provider/{interfaceName}/{url}

默认的consumer:  zookeeper路径为 /frank/dubbo/consumer/{interfaceName}/{url}

---
## 压测
环境 2019年macbook pro 
2.6 GHz 六核Intel Core i7
16GB内存
发压机和受压服务器，均为同一台机器，所以此结果仅供参考

### 一共6线程，同时发起rpc调用
1. 协议：netty，qps: 峰值41000+ qps，稳定在4w qps左右，压测持续5mins，无异常 cpu: 40%
2. 
3. 协议 http, qps: 峰值12000+ qps，1w qps左右，压测持续5mins，无异常 cpu: 70%

## 问题
当前版本的序列化协议仍为json，可能有性能瓶颈，未来将会优化为其他高性能序列化协议
# 如果有问题，请联系 xianchaolin@126.com