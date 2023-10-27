package com.lxc.dubbo.core.cache;

import com.lxc.dubbo.core.domain.ObjectInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalProviderCache {
    private static Map<String, ObjectInfo> cache = new HashMap<>();

    public static void register(String interfaceName, Class clazz, String beanName) {
        cache.put(interfaceName, new ObjectInfo(clazz, beanName));
    }

    public static ObjectInfo get(String interfaceName) {
        return cache.get(interfaceName);
    }

    public static Set<String> getAllInterfaces() {
        return cache.keySet();
    }
}
