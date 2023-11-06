package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.Invocation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FrankMiniDubboInvocationHandler extends AbstractFrankMiniDubboSerializeHandler<Invocation> {


    public FrankMiniDubboInvocationHandler() {
        super(Invocation.class);
    }
}
