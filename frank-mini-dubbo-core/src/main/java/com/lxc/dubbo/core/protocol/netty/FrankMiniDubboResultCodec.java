package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.FrankMiniDubboResultMessage;
import com.lxc.dubbo.core.domain.RequestResult;

public class FrankMiniDubboResultCodec extends FrankMiniDubboBaseCodec<FrankMiniDubboResultMessage, RequestResult> {

    public FrankMiniDubboResultCodec() {
        super(RequestResult.class);
    }
}
