package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.RequestResult;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class NettyClientHandler extends ChannelDuplexHandler {


    //当通道就绪就会触发该方法
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("consumer: {} 成功连接, provider: {}", channel.localAddress(), channel.remoteAddress());
    }

    //当通道有读取事件时，会触发
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RequestResult requestResult = (RequestResult) msg;
        DefaultFuture defaultFuture = DefaultFuture.getFUTURES().get(requestResult.getUuid());
        defaultFuture.complete(requestResult);
        DefaultFuture.getFUTURES().remove(requestResult.getUuid());
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
