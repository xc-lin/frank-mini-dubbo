# Frank mini dubbo

## frank mini dubbo 是一套简单基于http和springboot的rpc远程方法调用的开箱即用的框架，并使用zookeeper作为注册中心

1. 能实现负载均衡
2. 能实现服务的自动注册，以及下线
3. 能实现消费者像调用**本地方法**一样调用**远程方法**

---

下面向大家介绍各个模块的作用

1. dubbo-core是核心包，实现了rpc远程调用
2. dubbo-registry是注册中心包，主要负责服务的注册，以及本地服务缓存的操作等
3. dubbo-domain是实体类包
4. dubbo-demo为demo

默认的provider:  zookeeper路径为 /frank/dubbo/provider/{interfaceName}/{url}

默认的consumer:  zookeeper路径为 /frank/dubbo/consumer/{interfaceName}/{url}

# 如果有问题，请联系 xianchaolin@126.com