package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.FrankMiniDubboMessage;
import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.domain.result.RequestResult;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class NettyClient {

    private Channel socketChannel;


    public NettyClient(Url url) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel
                                    .pipeline()
                                    // 解决粘包问题
                                    .addLast(new LoggingHandler())
                                    .addLast(new LengthFieldBasedFrameDecoder(1000000, 12, 4, 0, 0))
                                    .addLast(new FrankMiniDubboResultCodec())
                                    .addLast(new FrankMiniDubboInvocationCodec())
                                    .addLast(new NettyClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(url.getHost(), Integer.parseInt(url.getPort())).sync();
            socketChannel = channelFuture.channel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RequestResult send(Invocation invocation, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        DefaultFuture defaultFuture = new DefaultFuture(socketChannel, invocation, 0);
//        log.info("NettyClient: {}", JSON.toJSONString(invocation));
        socketChannel.writeAndFlush(new FrankMiniDubboMessage(invocation, 0,0));
        return defaultFuture.get(timeout, timeUnit);
    }

    public void close() {
        socketChannel.close();
    }
}