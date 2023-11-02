package com.lxc.dubbo.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FrankMiniDubboMessage {

    private Invocation invocation;

    private int sequenceId;

    private int length;
}
