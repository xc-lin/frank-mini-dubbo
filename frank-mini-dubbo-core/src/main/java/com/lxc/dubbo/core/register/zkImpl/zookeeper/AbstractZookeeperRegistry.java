package com.lxc.dubbo.core.register.zkImpl.zookeeper;

import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.domain.enums.ProtocolConstants;
import com.lxc.dubbo.core.register.Registry;
import com.lxc.dubbo.core.cache.LocalConsumerCache;
import com.lxc.dubbo.core.protocol.netty.NettyClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Objects;

import static org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode.BUILD_INITIAL_CACHE;

@Slf4j
public abstract class AbstractZookeeperRegistry implements Registry {


    @Value("${protocol}")
    protected String protocol;

    @Autowired
    protected CuratorFramework client;

    @Override
    public void register(String interfaceName, Url url) {
        try {
            if (client.checkExists().forPath("/" + getPrefix() + "/" + interfaceName) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + getPrefix() + "/" + interfaceName);
            }
            String nodePath = client.create().withMode(CreateMode.EPHEMERAL).forPath(String.format("/" + getPrefix() + "/%s/%s", interfaceName, JSON.toJSONString(url)));
            log.info("frank mini dubbo register service: {} on dubbo node: {}", interfaceName, nodePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void logOff(String interfaceName, Url url) {
        try {
            client.delete().forPath(String.format("/" + getPrefix() + "/%s/%s", interfaceName, JSON.toJSONString(url)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getUrls(String interfaceName) {
        List<String> urlJsons = null;
        try {
            urlJsons = client.getChildren().forPath("/" + getPrefix() + "/" + interfaceName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String urlJson : urlJsons) {
            Url url = JSON.parseObject(urlJson, Url.class);
            LocalConsumerCache.set(interfaceName, url);
            if (Objects.equals(protocol, ProtocolConstants.NETTY)) {
                LocalConsumerCache.set(url, new NettyClient(url));
            }
        }
        log.info("接口: {}, url：{}存入本地缓存", interfaceName, urlJsons);
    }

    @Override
    public void watchInterface(String interfaceName) throws Exception {
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/" + getPrefix() + "/" + interfaceName, true);
        pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_ADDED)) {
                    String path = event.getData().getPath();
                    String urlJson = path.replace("/" + getPrefix() + "/" + interfaceName + "/", "");
                    Url url = JSON.parseObject(urlJson, Url.class);
                    LocalConsumerCache.set(interfaceName, url);
                    if (Objects.equals(protocol, ProtocolConstants.NETTY)) {
                        LocalConsumerCache.set(url, new NettyClient(url));
                    }
                    log.info("接口: {}，增加provider: {}", interfaceName, urlJson);
                } else if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    String path = event.getData().getPath();
                    String urlJson = path.replace("/" + getPrefix() + "/" + interfaceName + "/", "");
                    Url url = JSON.parseObject(urlJson, Url.class);
                    LocalConsumerCache.remove(interfaceName, url);
                    log.info("接口: {}，减少provider: {}", interfaceName, urlJson);

                    if (Objects.equals(protocol, ProtocolConstants.NETTY)) {
                        LocalConsumerCache.remove(url);
                    }



                }
            }
        });
        pathChildrenCache.start(BUILD_INITIAL_CACHE);

    }
}
