package com.lxc.dubbo.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ObjectInfo {
    /**
     * 类对象
     */
    private Class clazz;

    /**
     * spring中的bean名字
     */
    private String beanName;
}
