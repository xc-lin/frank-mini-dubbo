package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.util.NetUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class NettyServer {

    public static void startServer(int port) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            // 解决粘包 拆包问题
                            pipeline.addLast(new LoggingHandler());
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(100000000, 12, 4, 0, 0));
                            pipeline.addLast(new FrankMiniDubboInvocationCodec());
                            pipeline.addLast(new FrankMiniDubboResultCodec());
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });

            String hostAddress = NetUtil.getIpAddress();
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            log.info("当前服务器netty已启动, {}:{}", hostAddress, port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}