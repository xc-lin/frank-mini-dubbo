package com.lxc.dubbo.core.reflection;

import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.ObjectInfo;
import com.lxc.dubbo.core.domain.enums.ApiErrCodeExceptionEnum;
import com.lxc.dubbo.core.domain.enums.OverFlowLimitExceptionEnum;
import com.lxc.dubbo.core.domain.excetion.ApiErrCodeException;
import com.lxc.dubbo.core.cache.LocalProviderCache;
import com.lxc.dubbo.core.domain.excetion.OverFlowLimitException;
import com.lxc.dubbo.core.limit.FrankRateLimiter;
import com.lxc.dubbo.core.util.ApplicationContextUtil;
import com.lxc.dubbo.core.util.LogUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
public class MethodInvocation {
    public static Object providerMethodInvocation(Invocation invocation) throws ApiErrCodeException, IllegalAccessException, InvocationTargetException, OverFlowLimitException {
        FrankRateLimiter rateLimit = LocalProviderCache.getRateLimit(invocation.getInterfaceName());
        if (Objects.nonNull(rateLimit) && !rateLimit.tryAcquire()) {
            throw new OverFlowLimitException(OverFlowLimitExceptionEnum.OVER_FLOW_LIMIT_EXCEPTION.getCode(), String.format(OverFlowLimitExceptionEnum.OVER_FLOW_LIMIT_EXCEPTION.getMsg(), invocation.getInterfaceName(), rateLimit.getRate()));
        }
        ObjectInfo objectInfo = LocalProviderCache.get(invocation.getInterfaceName());
        if (Objects.isNull(objectInfo)) {
            LogUtil.error("interface未暴露到frank mini dubbo rpc调用中，interfaceName: {}", invocation.getInterfaceName());
            throw new ApiErrCodeException(ApiErrCodeExceptionEnum.INTERFACE_NOT_EXPORT);
        }
        LogUtil.debug("执行{}.{}", invocation.getInterfaceName(), invocation.getMethodName());
        Class clazz = objectInfo.getClazz();
        Method method = null;
        try {
            method = clazz.getMethod(invocation.getMethodName(), invocation.getParamTypes());

        } catch (NoSuchMethodException exception) {
            LogUtil.error("{}.{}方法未暴露到frank mini dubbo rpc调用中", invocation.getInterfaceName(), invocation.getMethodName());
            throw new ApiErrCodeException(ApiErrCodeExceptionEnum.INTERFACE_NOT_EXPORT);
        }
        if (Objects.isNull(method)) {
            LogUtil.error("{}.{}方法未暴露到frank mini dubbo rpc调用中", invocation.getInterfaceName(), invocation.getMethodName());
            throw new ApiErrCodeException(ApiErrCodeExceptionEnum.INTERFACE_NOT_EXPORT);
        }

        Object bean = ApplicationContextUtil.getSpringBeanByTypeAndId(objectInfo.getBeanName(), clazz);
        if (Objects.isNull(bean)) {
            LogUtil.error("接口: {}, beanName: {}, 未能在spring容器中找到", invocation.getInterfaceName(), objectInfo.getBeanName());
            throw new ApiErrCodeException(ApiErrCodeExceptionEnum.OBJECT_NOT_IN_SPRING);
        }
        Object result = method.invoke(bean, invocation.getParams());
        LogUtil.debug("result:{}", JSON.toJSONString(result));
        return result;
    }
}
