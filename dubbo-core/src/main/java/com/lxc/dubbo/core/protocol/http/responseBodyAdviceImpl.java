package com.lxc.dubbo.core.protocol.http;

import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.domain.result.RequestResult;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

@RestControllerAdvice(basePackages = "com.lxc.dubbo")
public class responseBodyAdviceImpl implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (Objects.equals(body.getClass(), RequestResult.class)){
            return body;
        }
        if (body instanceof String){
            return JSON.toJSONString(RequestResult.buildSuccess(body));
        }
        return RequestResult.buildSuccess(body);

    }
}
