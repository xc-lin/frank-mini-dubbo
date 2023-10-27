package com.lxc.dubbo.core.domain.enums;

public enum ApiErrCodeExceptionEnum {
    INTERFACE_NOT_EXPORT(1, "当前接口未暴露给rpc调用"),

    INTERFACE_METHOD_NOT_EXPORT(2, "当前接口的方法未暴露给rpc调用"),

    OBJECT_NOT_IN_SPRING(3, "当前bean未在spring中"),

    NO_ALIVE_PROVIDER(4,"没有存活的的提供者");

    ;
    private int code;
    private String msg;

    ApiErrCodeExceptionEnum(int code, String msg) {
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
