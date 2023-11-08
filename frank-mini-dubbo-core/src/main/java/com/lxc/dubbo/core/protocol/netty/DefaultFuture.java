package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.RequestResult;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFuture extends CompletableFuture<RequestResult> {

    /**
     * 通道
     */
    private final Channel channel;
    /**
     * 请求uuid 以及CompletableFuture对应存储
     */
    private static Map<String, DefaultFuture> FUTURES = new ConcurrentHashMap<>();

    public DefaultFuture(Channel channel, Invocation invocation, int timeout) {
        this.channel = channel;
        DefaultFuture put = FUTURES.put(invocation.getUuid(), this);
    }

    public Channel getChannel() {
        return channel;
    }

    public static Map<String, DefaultFuture> getFUTURES() {
        return FUTURES;
    }
}
