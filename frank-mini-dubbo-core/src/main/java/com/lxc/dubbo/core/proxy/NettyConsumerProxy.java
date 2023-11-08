package com.lxc.dubbo.core.proxy;


import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.annotaion.FrankDubboReference;
import com.lxc.dubbo.core.cache.LocalConsumerCache;
import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.RequestResult;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.domain.enums.ProtocolConstants;
import com.lxc.dubbo.core.domain.enums.SerializeTypeEnum;
import com.lxc.dubbo.core.protocol.netty.NettyClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Component
@ConditionalOnProperty(value = "protocol", havingValue = ProtocolConstants.NETTY)
public class NettyConsumerProxy extends AbstractConsumerProxy {

    public Object rpcExecute(Method method, Invocation invocation, Url url, FrankDubboReference frankDubboReference) throws TimeoutException, ExecutionException, InterruptedException {
        // 获取当前url对应的netty客户端，其中包含了通道，和连接
        NettyClient nettyClient = LocalConsumerCache.get(url);
        // 发送请求，并阻塞，直到请求返回结果,或超时(这里的超时是用futrue.get来实现的超时)
        RequestResult requestResult = nettyClient.send(invocation, frankDubboReference.timeout(), frankDubboReference.timeUnit());
        if (requestResult.isSuccess()) {
            if (method.getReturnType() == String.class) {
                return requestResult.getData();
            }

            // 如果序列化方式是json，还需要将返回结果反序列化一次
            if (Objects.equals(serializeType, SerializeTypeEnum.JSON.getName())) {
                return JSON.toJavaObject((JSON) requestResult.getData(), method.getReturnType());
            }
            return requestResult.getData();
        }
//                    LogUtil.error("提供者返回接口错误: {}", JSON.toJSONString(requestResult));
        throw new RuntimeException(requestResult.getMessage());
    }
}
