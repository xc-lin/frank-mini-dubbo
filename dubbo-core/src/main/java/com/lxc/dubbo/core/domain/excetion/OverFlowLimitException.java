package com.lxc.dubbo.core.domain.excetion;


public class OverFlowLimitException extends Exception {
    private int errCode;
    private String errMsg;

    public OverFlowLimitException() {
    }

    public OverFlowLimitException(String message) {
        super(message);
    }

    public OverFlowLimitException(int errCode, String errMsg) {
        super(String.format("errCode:%s,errMsg:%s", errCode, errMsg));
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

}
