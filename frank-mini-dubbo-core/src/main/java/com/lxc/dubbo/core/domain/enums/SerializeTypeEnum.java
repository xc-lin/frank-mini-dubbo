package com.lxc.dubbo.core.domain.enums;

import java.util.Objects;

public enum SerializeTypeEnum {
    JSON(0, "json"),
    HESSIAN(1, "hessian"),

    ;
    int code;

    String name;

    SerializeTypeEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static SerializeTypeEnum getByName(String name) {
        SerializeTypeEnum[] values = SerializeTypeEnum.values();
        for (SerializeTypeEnum value : values) {
            if (Objects.equals(name, value.getName())) {
                return value;
            }
        }
        return JSON;
    }
}
