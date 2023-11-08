package com.lxc.dubbo.core.register.zkImpl.zookeeper;

import com.alibaba.fastjson.JSON;
import com.lxc.dubbo.core.cache.LocalConsumerCache;
import com.lxc.dubbo.core.domain.Url;
import com.lxc.dubbo.core.domain.constants.RegistryConstant;
import com.lxc.dubbo.core.domain.enums.ProtocolConstants;
import com.lxc.dubbo.core.protocol.netty.NettyClient;
import com.lxc.dubbo.core.protocol.netty.NettyServer;
import com.lxc.dubbo.core.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class ProviderZookeeperRegistry extends AbstractZookeeperRegistry {


    @Override
    public void register(String interfaceName, Url url) {
        try {
            if (client.checkExists().forPath("/" + getPrefix() + "/" + interfaceName) == null) {
                // 创建永久节点
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + getPrefix() + "/" + interfaceName);
            }
            // 创建临时节点，当机器下线，zookeeper则自动删除当前节点
            String nodePath = client.create().withMode(CreateMode.EPHEMERAL).forPath(String.format("/" + getPrefix() + "/%s/%s", interfaceName, JSON.toJSONString(url)));
            LogUtil.info("frank mini dubbo register service: {} on dubbo node: {}", interfaceName, nodePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getUrls(String interfaceName) {
        List<String> urlJsons = null;
        try {
            // 获取当前接口所有的url
            urlJsons = client.getChildren().forPath("/" + getPrefix() + "/" + interfaceName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String urlJson : urlJsons) {
            Url url = JSON.parseObject(urlJson, Url.class);
            // 将接口的provider list 存入本地
            LocalConsumerCache.set(interfaceName, url);
            if (Objects.equals(protocol, ProtocolConstants.NETTY)) {
                // consumer启动netty客户端与provider连接
                LocalConsumerCache.set(url, new NettyClient(url));
            }
        }
        LogUtil.info("接口: {}, url：{}存入本地缓存", interfaceName, urlJsons);
    }

    @Override
    public String getPrefix() {
        return RegistryConstant.PROVIDER + "/" + protocol;
    }
}
