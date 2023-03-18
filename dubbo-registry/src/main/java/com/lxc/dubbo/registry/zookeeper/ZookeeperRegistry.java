package com.lxc.dubbo.registry.zookeeper;

import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.domain.Url;
import com.lxc.dubbo.registry.Registry;
import com.lxc.dubbo.registry.cache.LocalCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode.BUILD_INITIAL_CACHE;

@Component
@Slf4j
public class ZookeeperRegistry implements Registry {

    @Autowired
    private CuratorFramework client;

    @Override
    public void register(String interfaceName, Url url) throws Exception {
        try {
            if (client.checkExists().forPath("/" + interfaceName) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + interfaceName);
            }
            String nodePath = client.create().withMode(CreateMode.EPHEMERAL).forPath(String.format("/%s/%s", interfaceName, JSON.toJSONString(url)));
            log.info("frank mini dubbo register service: {} on dubbo node: {}", interfaceName, nodePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void logOff(String interfaceName, Url url) throws Exception {
        client.delete().forPath(String.format("/%s/%s", interfaceName, JSON.toJSONString(url)));
    }

    @Override
    public void getUrls(String interfaceName) throws Exception {
        List<String> urlJsons = client.getChildren().forPath("/" + interfaceName);
        for (String urlJson : urlJsons) {
            Url url = JSON.parseObject(urlJson, Url.class);
            LocalCache.set(interfaceName, url);
        }
        log.info("接口: {}, url：{}存入本地缓存", interfaceName, urlJsons);
    }

    @Override
    public void watchInterface(String interfaceName) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/" + interfaceName, true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                    String path = event.getData().getPath();
                    String urlJson = path.replace("/" + interfaceName + "/", "");
                    LocalCache.set(interfaceName, JSON.parseObject(urlJson, Url.class));
                    log.info("接口: {}，增加provider: {}", interfaceName, urlJson);
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    String path = event.getData().getPath();
                    String urlJson = path.replace("/" + interfaceName + "/", "");
                    LocalCache.remove(interfaceName, JSON.parseObject(urlJson, Url.class));
                    log.info("接口: {}，减少provider: {}", interfaceName, urlJson);
                }
            }
        });
        pathChildrenCache.start(BUILD_INITIAL_CACHE);

    }
}
