package com.lxc.dubbo.core.domain;

import com.lxc.dubbo.core.domain.result.RequestResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrankMiniDubboResultMessage {

    private int sequenceId;

    private int length;

    private RequestResult requestResult;
}
