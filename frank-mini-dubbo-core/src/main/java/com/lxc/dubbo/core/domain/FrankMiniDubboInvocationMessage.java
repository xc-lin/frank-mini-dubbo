package com.lxc.dubbo.core.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FrankMiniDubboInvocationMessage extends FrankMiniDubboBaseMessage<Invocation>{

    public FrankMiniDubboInvocationMessage(int sequenceId, int length, int serializeType, Invocation invocation) {
        super(sequenceId, length, serializeType, invocation);
    }
}
