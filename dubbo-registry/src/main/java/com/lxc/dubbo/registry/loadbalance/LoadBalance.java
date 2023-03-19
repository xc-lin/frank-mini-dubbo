package com.lxc.dubbo.registry.loadbalance;

import com.lxc.dubbo.domain.Url;

import java.util.List;

public interface LoadBalance {
    Url getUrl(List<Url> urls);
}
