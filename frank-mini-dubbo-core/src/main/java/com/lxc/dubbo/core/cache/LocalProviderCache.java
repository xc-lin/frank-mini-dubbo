package com.lxc.dubbo.core.cache;

import com.lxc.dubbo.core.annotaion.FrankDubbo;
import com.lxc.dubbo.core.domain.ObjectInfo;
import com.lxc.dubbo.core.limit.FrankRateLimiter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocalProviderCache {
    private static Map<String, ObjectInfo> cache = new HashMap<>();

    private static Map<String, FrankRateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    public static void register(String interfaceName, Class clazz, String beanName, FrankDubbo frankDubbo) {
        cache.put(interfaceName, new ObjectInfo(clazz, beanName));
        if (frankDubbo.flowLimit() > 0) {
            rateLimiterMap.put(interfaceName, new FrankRateLimiter(frankDubbo.flowLimit()));
        }
    }

    public static ObjectInfo get(String interfaceName) {
        return cache.get(interfaceName);
    }

    public static FrankRateLimiter getRateLimit(String interfaceName) {
        return rateLimiterMap.get(interfaceName);
    }


    public static Set<String> getAllInterfaces() {
        return cache.keySet();
    }
}
