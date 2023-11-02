package com.lxc.dubbo.core.domain.enums;

public enum OverFlowLimitExceptionEnum {
    OVER_FLOW_LIMIT_EXCEPTION(6666, "%s 接口已超过限流值: %s"),

    ;
    private int code;
    private String msg;

    OverFlowLimitExceptionEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
