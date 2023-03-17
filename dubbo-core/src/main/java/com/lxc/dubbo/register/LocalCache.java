package com.lxc.dubbo.register;

import java.util.HashMap;
import java.util.Map;

public class LocalCache {
    private static Map<String, Class> cache = new HashMap<>();

    public static void register(String interfaceName, Class clazz){
        cache.put(interfaceName, clazz);
    }

    public static Class get(String interfaceName){
        return cache.get(interfaceName);
    }
}
