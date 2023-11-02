package com.lxc.dubbo.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FrankMiniDubboMessage extends FrankMiniDubboBaseMessage{

    private Invocation invocation;

    public FrankMiniDubboMessage(int sequenceId, int length, int serializeType, Invocation invocation) {
        super(sequenceId, length, serializeType);
        this.invocation = invocation;
    }
}
