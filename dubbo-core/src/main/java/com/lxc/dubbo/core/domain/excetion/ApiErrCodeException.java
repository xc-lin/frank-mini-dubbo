package com.lxc.dubbo.core.domain.excetion;

import com.lxc.dubbo.core.domain.enums.ApiErrCodeExceptionEnum;

public class ApiErrCodeException extends Exception {
    private int errCode;
    private String errMsg;

    public ApiErrCodeException() {
    }

    public ApiErrCodeException(String message) {
        super(message);
    }

    public ApiErrCodeException(int errCode, String errMsg) {
        super(String.format("errCode:%s,errMsg:%s", errCode, errMsg));
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public ApiErrCodeException(ApiErrCodeExceptionEnum apiErrCodeExceptionEnum) {
        super(String.format("errCode:%s,errMsg:%s", apiErrCodeExceptionEnum.getCode(), apiErrCodeExceptionEnum.getMsg()));
        this.errCode = apiErrCodeExceptionEnum.getCode();
        this.errMsg = apiErrCodeExceptionEnum.getMsg();
    }

}
