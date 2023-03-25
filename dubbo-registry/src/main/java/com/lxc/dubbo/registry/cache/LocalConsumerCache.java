package com.lxc.dubbo.registry.cache;

import com.lxc.dubbo.domain.Url;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Set;

public class LocalConsumerCache {
    private static MultiValueMap<String, Url> interfaceUrlCache = new LinkedMultiValueMap<>();

    public static void set(String interfaceName, Url url) {
        interfaceUrlCache.add(interfaceName, url);
    }

    public static List<Url> get(String interfaceName) {
        return interfaceUrlCache.get(interfaceName);
    }

    public static void remove(String interfaceName, Url url) {
        List<Url> urls = get(interfaceName);
        urls.remove(url);
    }

    public static Set<String> getAllInterfaces() {
        return interfaceUrlCache.keySet();
    }
}
