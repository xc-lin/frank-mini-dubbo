package com.lxc.dubbo.core.protocol.netty;

import com.alibaba.fastjson.JSON;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.lxc.dubbo.core.domain.FrankMiniDubboBaseMessage;
import com.lxc.dubbo.core.domain.FrankMiniDubboProtocol;
import com.lxc.dubbo.core.domain.RequestResult;
import com.lxc.dubbo.core.domain.enums.SerializeTypeEnum;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
public class FrankMiniDubboResponseSerializeHandler extends AbstractFrankMiniDubboSerializeHandler<RequestResult> {


    public FrankMiniDubboResponseSerializeHandler() {
        super(RequestResult.class);
    }
}
