package com.lxc.dubbo.core.advice;

import com.lxc.dubbo.core.excetion.ApiErrCodeException;
import com.lxc.dubbo.domain.result.RequestResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice("com.lxc.dubbo.core.invocation")
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
