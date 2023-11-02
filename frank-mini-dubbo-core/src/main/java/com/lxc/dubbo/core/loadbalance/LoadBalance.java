package com.lxc.dubbo.core.loadbalance;

import com.lxc.dubbo.core.domain.Url;

import java.util.List;

public interface LoadBalance {
    Url getUrl(List<Url> urls);
}
