package com.lxc.domain;

import lombok.Data;

@Data
public class Invocation {
    private String interfaceName;

    private String methodName;

    private Object[] params;

    private Class[] paramTypes;
}
