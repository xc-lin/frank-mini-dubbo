package com.lxc.dubbo.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrankMiniDubboResultMessage extends FrankMiniDubboBaseMessage<RequestResult>{

    private RequestResult requestResult;

    public FrankMiniDubboResultMessage(int sequenceId, int length, int serializeType, RequestResult requestResult) {
        super(sequenceId, length, serializeType, requestResult);
    }
}
