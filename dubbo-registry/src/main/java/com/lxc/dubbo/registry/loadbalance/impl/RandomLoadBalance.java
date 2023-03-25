package com.lxc.dubbo.registry.loadbalance.impl;

import cn.hutool.core.util.RandomUtil;
import com.lxc.dubbo.domain.Url;
import com.lxc.dubbo.registry.loadbalance.LoadBalance;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomLoadBalance implements LoadBalance {


    @Override
    public Url getUrl(List<Url> urls) {
        return RandomUtil.randomEle(urls);
    }
}
