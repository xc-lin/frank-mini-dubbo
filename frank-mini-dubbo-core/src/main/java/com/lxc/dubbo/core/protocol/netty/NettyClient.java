package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.FrankMiniDubboInvocationMessage;
import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.RequestResult;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.domain.enums.SerializeTypeEnum;
import com.lxc.dubbo.core.util.ApplicationContextUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
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
                                    .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 12, 4, 0, 0))
                                    // 解析自己的协议
                                    .addLast(new FrankMiniDubboCodec())
                                    // Invocation序列化/反序列化
                                    .addLast(new FrankMiniDubboInvocationHandler())
                                    // Response序列化/反序列化
                                    .addLast(new FrankMiniDubboResponseSerializeHandler())
                                    // 当通道有读取事件时, 获取completableFuture，调用complete 唤醒等待的线程
                                    .addLast(new NettyClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(url.getHost(), Integer.parseInt(url.getPort())).sync();
            socketChannel = channelFuture.channel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送网络请求
     * @param invocation
     * @param timeout
     * @param timeUnit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    public RequestResult send(Invocation invocation, long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        // 创建一个completableFuture，用来等待provider返回数据
        DefaultFuture defaultFuture = new DefaultFuture(socketChannel, invocation, 0);
        // log.info("NettyClient: {}", JSON.toJSONString(invocation));
        String serializeTypeInSystem = ApplicationContextUtil.getContext().getEnvironment().getProperty("serializeType");
        // 获取序列化方式
        int serializeType = SerializeTypeEnum.getByName(serializeTypeInSystem).getCode();
        // 发送请求
        socketChannel.writeAndFlush(new FrankMiniDubboInvocationMessage(0, 0, serializeType, invocation));
        // 阻塞等待provider返回
        return defaultFuture.get(timeout, timeUnit);
    }

    public void close() {
        socketChannel.close();
    }
}
