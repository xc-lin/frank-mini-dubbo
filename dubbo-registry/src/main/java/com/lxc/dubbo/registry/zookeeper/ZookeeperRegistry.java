package com.lxc.dubbo.registry.zookeeper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lxc.dubbo.domain.Url;
import com.lxc.dubbo.registry.Registry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class ZookeeperRegistry implements Registry {

    @Autowired
    private CuratorFramework client;

    @Override
    public void register(String interfaceName, Url url) throws Exception {
        try {
            String s = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(String.format("/%s/%s", interfaceName, JSON.toJSONString(url)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void logOff(String interfaceName, Url url) throws Exception {
        client.delete().forPath(String.format("/%s/%s", interfaceName, JSON.toJSONString(url)));
    }
}
