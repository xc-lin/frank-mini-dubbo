package com.lxc.dubbo.core.protocol.netty;

import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.result.RequestResult;
import com.lxc.dubbo.core.reflection.MethodInvocation;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class NettyServerHandler extends ChannelDuplexHandler {


    //当通道就绪就会触发该方法
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("provider: {} 成功连接, consumer: {}", channel.localAddress(), channel.remoteAddress());
    }

    //当通道有读取事件时，会触发
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Invocation invocation = JSON.parseObject(msg.toString(), Invocation.class);
        Object o = null;
        RequestResult requestResult = null;
        try {
            o = MethodInvocation.providerMethodInvocation(invocation);
            requestResult = RequestResult.buildSuccess(o);
        } catch (Exception e) {
            requestResult  = RequestResult.buildFailure(e);
        }
        requestResult.setUuid(invocation.getUuid());
        ctx.writeAndFlush(JSON.toJSONString(requestResult));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
