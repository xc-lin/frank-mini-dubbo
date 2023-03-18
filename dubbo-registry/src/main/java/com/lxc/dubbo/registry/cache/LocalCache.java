package com.lxc.dubbo.registry.cache;

import com.lxc.dubbo.domain.Url;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

public class LocalCache {
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
}
