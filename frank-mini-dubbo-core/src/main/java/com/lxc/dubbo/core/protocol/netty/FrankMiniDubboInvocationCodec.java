package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.FrankMiniDubboMessage;
import com.lxc.dubbo.core.domain.FrankMiniDubboResultMessage;
import com.lxc.dubbo.core.domain.Invocation;

public class FrankMiniDubboInvocationCodec extends FrankMiniDubboBaseCodec<FrankMiniDubboMessage, Invocation> {

    public FrankMiniDubboInvocationCodec() {
        super(Invocation.class);
    }
}
