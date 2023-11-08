package com.lxc.dubbo.core.proxy;


import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.annotaion.FrankDubboReference;
import com.lxc.dubbo.core.domain.enums.ProtocolConstants;
import com.lxc.dubbo.core.domain.enums.SerializeTypeEnum;
import com.lxc.dubbo.core.domain.excetion.ApiErrCodeException;
import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.domain.constants.UrlConstant;
import com.lxc.dubbo.core.domain.RequestResult;
import com.lxc.dubbo.core.cache.LocalConsumerCache;
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
import java.util.concurrent.TimeoutException;

import static com.lxc.dubbo.core.domain.enums.ApiErrCodeExceptionEnum.NO_ALIVE_PROVIDER;

@Component
@Slf4j
@ConditionalOnProperty(value = "protocol", havingValue = ProtocolConstants.HTTP)
public class HttpConsumerProxy extends AbstractConsumerProxy {


    @Override
    public Object rpcExecute(Method method, Invocation invocation, Url url, FrankDubboReference frankDubboReference) {
        // 获取注解上的超时时间
        long timeoutMillis = frankDubboReference.timeUnit().toMillis(frankDubboReference.timeout());
        // 发送http请求
        String result = HttpUtil.post(url.getAddressAndPort() + UrlConstant.RPC_URL, JSON.toJSONString(invocation), (int) timeoutMillis);
        // 解析反序列化结果
        RequestResult requestResult = JSON.parseObject(result, RequestResult.class);
        if (requestResult.isSuccess()) {
            if (method.getReturnType() == String.class) {
                return requestResult.getData();
            }
            return JSON.toJavaObject((JSON) requestResult.getData(), method.getReturnType());
        }
//        LogUtil.error("提供者返回接口错误: {}", JSON.toJSONString(requestResult));
        throw new RuntimeException(requestResult.getMessage());

    }
}
