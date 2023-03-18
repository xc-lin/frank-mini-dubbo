package com.lxc.dubbo.register;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalCache {
    private static Map<String, Class> cache = new HashMap<>();

    public static void register(String interfaceName, Class clazz){
        cache.put(interfaceName, clazz);
    }

    public static Class get(String interfaceName){
        return cache.get(interfaceName);
    }

    public static Set<String> getAllInterfaces(){
        return cache.keySet();
    }
}
