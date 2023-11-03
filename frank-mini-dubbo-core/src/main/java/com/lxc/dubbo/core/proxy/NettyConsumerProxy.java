package com.lxc.dubbo.core.proxy;


import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.annotaion.FrankDubboReference;
import com.lxc.dubbo.core.domain.enums.ProtocolConstants;
import com.lxc.dubbo.core.domain.enums.SerializeTypeEnum;
import com.lxc.dubbo.core.domain.excetion.ApiErrCodeException;
import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.domain.RequestResult;
import com.lxc.dubbo.core.cache.LocalConsumerCache;
import com.lxc.dubbo.core.protocol.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.lxc.dubbo.core.domain.enums.ApiErrCodeExceptionEnum.NO_ALIVE_PROVIDER;

@Component
@Slf4j
@ConditionalOnProperty(value = "protocol", havingValue = ProtocolConstants.NETTY)
public class NettyConsumerProxy extends AbstractConsumerProxy {


    public Object rpcExecute(Method method, Invocation invocation, Url url, FrankDubboReference frankDubboReference) throws TimeoutException, ExecutionException, InterruptedException {
        NettyClient nettyClient = LocalConsumerCache.get(url);
        RequestResult requestResult = nettyClient.send(invocation, frankDubboReference.timeout(), frankDubboReference.timeUnit());
        if (requestResult.isSuccess()) {
            if (method.getReturnType() == String.class) {
                return requestResult.getData();
            }

            if (Objects.equals(serializeType, SerializeTypeEnum.JSON.getName())) {
                return JSON.toJavaObject((JSON) requestResult.getData(), method.getReturnType());
            }
            return requestResult.getData();
        }
//                    LogUtil.error("提供者返回接口错误: {}", JSON.toJSONString(requestResult));
        throw new RuntimeException(requestResult.getMessage());
    }
}
