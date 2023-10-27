package com.lxc.dubbo.core.loadbalance.impl;

import cn.hutool.core.util.RandomUtil;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.loadbalance.LoadBalance;

import java.util.List;

public class RandomLoadBalance implements LoadBalance {


    @Override
    public Url getUrl(List<Url> urls) {
        return RandomUtil.randomEle(urls);
    }
}
