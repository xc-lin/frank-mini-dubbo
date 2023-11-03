package com.lxc.dubbo.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrankMiniDubboBaseMessage<T> implements Serializable {

    private int sequenceId;

    private int length;

    private int serializeType;

    private T data;
}
