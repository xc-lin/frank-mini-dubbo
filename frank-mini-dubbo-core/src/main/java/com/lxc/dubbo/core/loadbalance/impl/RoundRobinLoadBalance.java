package com.lxc.dubbo.core.loadbalance.impl;

import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.loadbalance.LoadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance implements LoadBalance {

    private static AtomicInteger num = new AtomicInteger(0);

    @Override
    public Url getUrl(List<Url> urls) {
        int n = num.incrementAndGet();
        if (n > urls.size() * 2) {
            num.compareAndSet(n, n % urls.size());
        }
        return urls.get(n % urls.size());
    }
}
