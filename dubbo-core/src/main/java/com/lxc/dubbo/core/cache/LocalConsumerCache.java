package com.lxc.dubbo.core.cache;

import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.protocol.netty.NettyClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalConsumerCache {
    private static MultiValueMap<String, Url> interfaceUrlCache = new LinkedMultiValueMap<>();
    private static ConcurrentHashMap<Url, NettyClient> urlNettyClient = new ConcurrentHashMap<>();


    public static void set(String interfaceName, Url url) {
        interfaceUrlCache.add(interfaceName, url);
    }

    public static void set(Url url,  NettyClient nettyClient) {
        urlNettyClient.put(url, nettyClient);
    }


    public static List<Url> get(String interfaceName) {
        return interfaceUrlCache.get(interfaceName);
    }

    public static NettyClient get(Url url) {
        return urlNettyClient.get(url);
    }

    public static void remove(String interfaceName, Url url) {
        List<Url> urls = get(interfaceName);
        urls.remove(url);
    }
    public static void remove(Url url) {
        NettyClient nettyClient = urlNettyClient.get(url);
        if (Objects.nonNull(nettyClient)) {
            nettyClient.close();
        }
        urlNettyClient.remove(url);
    }

    public static Set<String> getAllInterfaces() {
        return interfaceUrlCache.keySet();
    }
}
