package com.lxc.dubbo.core.protocol.netty;

import com.lxc.dubbo.core.domain.Invocation;
import com.lxc.dubbo.core.domain.result.RequestResult;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultFuture extends CompletableFuture<RequestResult> {
    private final Channel channel;
    private static Map<String, DefaultFuture> FUTURES = new HashMap<String, DefaultFuture>();

    public DefaultFuture(Channel channel, Invocation invocation, int timeout) {
        this.channel = channel;
        FUTURES.put(invocation.getUuid(), this);
    }

    public Channel getChannel() {
        return channel;
    }

    public static Map<String, DefaultFuture> getFUTURES() {
        return FUTURES;
    }
}
