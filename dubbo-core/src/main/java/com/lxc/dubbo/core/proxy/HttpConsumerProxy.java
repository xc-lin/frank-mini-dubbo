package com.lxc.dubbo.core.proxy;


import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.annotaion.FrankDubboReference;
import com.lxc.dubbo.core.domain.excetion.ApiErrCodeException;
import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.domain.constants.UrlConstant;
import com.lxc.dubbo.core.domain.result.RequestResult;
import com.lxc.dubbo.core.cache.LocalConsumerCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static com.lxc.dubbo.core.domain.enums.ApiErrCodeExceptionEnum.NO_ALIVE_PROVIDER;

@Component
@Slf4j
@ConditionalOnProperty(value = "protocol", havingValue = "http")
public class HttpConsumerProxy extends AbstractConsumerProxy {

    @Override
    public Object getProxy(Class interfaceClass, FrankDubboReference frankDubboReference) {
        return Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Invocation invocation = new Invocation(interfaceClass.getName(), method.getName(), args, method.getParameterTypes(), UUID.randomUUID().toString());
                List<Url> urls = LocalConsumerCache.get(interfaceClass.getName());
                if (CollectionUtils.isEmpty(urls)) {
                    log.error("当前接口: {}，没有存活的提供者: {}", interfaceClass.getName(), urls);
                    throw new ApiErrCodeException(NO_ALIVE_PROVIDER);
                }
                log.debug("当前接口: {}，存活的提供者: {}", interfaceClass.getName(), urls.toString());
                Url url = loadBalance.getUrl(urls);
                log.debug("当前接口: {}，选择: {}", interfaceClass.getName(), url);
                try {
                    long timeoutMillis = frankDubboReference.timeUnit().toMillis(frankDubboReference.timeout());
                    String result = HttpUtil.post(url.getAddressAndPort() + UrlConstant.RPC_URL, JSON.toJSONString(invocation), (int) timeoutMillis);
                    RequestResult requestResult = JSON.parseObject(result, RequestResult.class);
                    if (requestResult.isSuccess()) {
                        if (method.getReturnType() == String.class) {
                            return requestResult.getData();
                        }
                        return JSON.toJavaObject((JSON) requestResult.getData(), method.getReturnType());
                    }
                    log.error("提供者返回接口错误: {}", JSON.toJSONString(requestResult));
                    throw new RuntimeException(requestResult.getMessage());
                } catch (HttpException exception) {
                    throw new TimeoutException(String.format("failed to call %s on remote server %s, Timeout: %s", invocation.getInterfaceName(), url.getAddressAndPort(), frankDubboReference.timeUnit().toMillis(frankDubboReference.timeout())));
                }
            }
        });
    }
}
