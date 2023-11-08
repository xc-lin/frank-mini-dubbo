package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.enums.ProtocolConstants;
import com.lxc.dubbo.core.util.LogUtil;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@ConditionalOnProperty(value = "protocol", havingValue = ProtocolConstants.NETTY)
public class NettyServer implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${frank.dubbo.netty.port}")
    private String nettyPort;

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
                            socketChannel.pipeline()
                                    // 解决粘包问题
                                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 12, 4, 0, 0))
                                    // 解析自己的协议
                                    .addLast(new FrankMiniDubboCodec())
                                    // Invocation序列化/反序列化
                                    .addLast(new FrankMiniDubboInvocationHandler())
                                    // Response序列化/反序列化
                                    .addLast(new FrankMiniDubboResponseSerializeHandler())
                                    // 反射调用
                                    .addLast(new NettyServerHandler());
                        }
                    });

            String hostAddress = NetUtil.getIpAddress();
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            LogUtil.info("当前服务器netty已启动, {}:{}", hostAddress, port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 启动netty服务器
        NettyServer.startServer(Integer.parseInt(nettyPort));
        LogUtil.info("NettyServer已启动!");
    }
}
