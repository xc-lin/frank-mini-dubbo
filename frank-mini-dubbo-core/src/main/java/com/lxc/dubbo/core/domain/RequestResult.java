package com.lxc.dubbo.core.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class RequestResult<T> implements Serializable {

    private boolean success;

    private String message;

    private int errCode;

    private String errMsg;

    private String uuid;

    private T data;

    public static <T> RequestResult buildSuccess(T data){
        RequestResult<T> requestResult = new RequestResult();
        requestResult.setSuccess(true);
        requestResult.setData(data);
        return requestResult;
    }

    public static RequestResult buildSuccess(){
        RequestResult requestResult = new RequestResult();
        requestResult.setSuccess(true);
        return requestResult;
    }

    public static RequestResult buildFailure(Exception e){
        RequestResult requestResult = new RequestResult();
        requestResult.setSuccess(false);
        requestResult.setMessage(e.getMessage());
        return requestResult;
    }
}
