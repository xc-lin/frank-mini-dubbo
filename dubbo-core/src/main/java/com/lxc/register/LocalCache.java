package com.lxc.register;

import java.util.HashMap;
import java.util.Map;

public class LocalCache {
    private static Map<String, Class> cache = new HashMap<>();

    public static void register(Class clazz){
        cache.put(clazz.getName(), clazz);
    }

    public static Class get(String interfaceName){
        return cache.get(interfaceName);
    }
}
