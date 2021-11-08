package com.huawei.apm.core.service.configServer.zookeeper;

import com.huawei.apm.core.common.ConfigServerConfig;
import com.huawei.apm.core.config.ConfigLoader;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ZookeeperServerImpl implements ZookeeperServer {

    private static final Logger LOGGER = LogFactory.getLogger();

    private static CuratorFramework zkClient;

    private static final ConfigServerConfig configServerConfig = ConfigLoader.getConfig(ConfigServerConfig.class);

    @Override
    public void start() {
        try {
            zkClient = CuratorFrameworkFactory.builder()
                    .connectString(configServerConfig.getZkAddress())
                    .sessionTimeoutMs(Integer.parseInt(configServerConfig.getZkTimeout()))
                    .retryPolicy(new ExponentialBackoffRetry(Integer.parseInt(configServerConfig.getZkSleepTime()), Integer.parseInt(configServerConfig.getZkRetryTime())))
                    .build();
            zkClient.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.toString());
        }
    }

    @Override
    public void stop() {
        zkClient.close();
    }

    @Override
    public CuratorFramework getClient() {
        return zkClient;
    }
}
