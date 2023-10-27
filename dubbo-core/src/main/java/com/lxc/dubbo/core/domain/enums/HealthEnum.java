package com.lxc.dubbo.core.domain.enums;

public enum HealthEnum {

    HEALTH(1);

    Integer status;

    HealthEnum(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
