package com.lxc.dubbo.core.protocol.http;

import com.lxc.dubbo.core.domain.enums.ProtocolConstants;
import com.lxc.dubbo.core.domain.excetion.ApiErrCodeException;
import com.lxc.dubbo.core.domain.result.RequestResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice("com.lxc.dubbo.core.protocol.http")
@ConditionalOnProperty(value = "protocol", havingValue = ProtocolConstants.HTTP)
public class ExceptionAdvice {

    @ExceptionHandler(ApiErrCodeException.class)
    public RequestResult apiExceptionHandler(Exception e){
        return RequestResult.buildFailure(e);
    }

    @ExceptionHandler(Exception.class)
    public RequestResult exceptionHandler(Exception e){
        return RequestResult.buildFailure(e);
    }
}
