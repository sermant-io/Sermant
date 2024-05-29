/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.sermant.implement.service.dynamicconfig.zookeeper;


import io.sermant.core.common.CommonConstant;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.notification.NotificationInfo;
import io.sermant.core.notification.NotificationManager;
import io.sermant.core.notification.ZookeeperNotificationType;
import io.sermant.core.service.dynamicconfig.config.DynamicConfig;
import io.sermant.core.utils.AesUtil;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.Closeable;
import java.util.List;
import java.util.logging.Logger;

/**
 * {@link ZooKeeper} wrapper, which wraps the ZooKeeper native apis and provides easier apis to use
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-15
 */
public class ZooKeeperBufferedClient implements Closeable {
    /**
     * ZK path separator
     */
    public static final char ZK_PATH_SEPARATOR = '/';

    /**
     * ZK authorization separator
     */
    public static final char ZK_AUTH_SEPARATOR = ':';

    /**
     * Dynamic configuration information
     */
    private static final DynamicConfig CONFIG = ConfigManager.getConfig(DynamicConfig.class);

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String SCHEME = "digest";

    /**
     * ZK client
     */
    private ZooKeeperClient zkClient;

    /**
     * Create a ZooKeeperBufferedClient, initialize the ZK client, and provide an expired reconnection mechanism
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @throws ZooKeeperInitException In the case of dependent dynamic configuration, if ZK initialization fails then
     *                                Sermant needs to be interrupted
     */
    public ZooKeeperBufferedClient(String connectString, int sessionTimeout) {
        zkClient = new ZooKeeperClient(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                // 连接过期重连
                if (event.getState() == Event.KeeperState.Expired) {
                    zkClient = new ZooKeeperClient(connectString, sessionTimeout, this);
                }
                postZookeeperConnectNotification(event);
            }
        });
        checkConnect();
    }

    /**
     * Create a ZooKeeperBufferedClient, initialize the ZK client, and provide an expired reconnection mechanism
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @param userName username
     * @param password encrypted password
     * @param key key for encryption
     */
    public ZooKeeperBufferedClient(String connectString, int sessionTimeout, String userName,
                                   String password, String key) {
        String authInfo = userName + ZK_AUTH_SEPARATOR + AesUtil.decrypt(key, password).orElse(null);
        zkClient = new ZooKeeperClient(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                // The connection expires and reconnects
                if (event.getState() == Event.KeeperState.Expired) {
                    zkClient = new ZooKeeperClient(connectString, sessionTimeout, this);
                    waitConnect();
                    zkClient.addAuthInfo(SCHEME, authInfo.getBytes(CommonConstant.DEFAULT_CHARSET));
                }
                postZookeeperConnectNotification(event);
            }
        });
        checkConnect();
        zkClient.addAuthInfo(SCHEME, authInfo.getBytes(CommonConstant.DEFAULT_CHARSET));
    }

    /**
     * Send zookeeper connection notifications
     *
     * @param event zookeeper WatchedEvent
     */
    private static void postZookeeperConnectNotification(WatchedEvent event) {
        if (NotificationManager.isEnable()) {
            if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                NotificationManager.doNotify(new NotificationInfo(ZookeeperNotificationType.CONNECTED, null));
            } else if (event.getState() == Watcher.Event.KeeperState.Disconnected) {
                NotificationManager.doNotify(new NotificationInfo(ZookeeperNotificationType.DISCONNECTED, null));
            }
        }
    }

    /**
     * Check connection
     *
     * @throws ZooKeeperInitException zk initialization exception
     */
    private void checkConnect() {
        waitConnect();
        if (zkClient.getState() != ZooKeeper.States.CONNECTED
                && zkClient.getState() != ZooKeeper.States.CONNECTEDREADONLY) {
            throw new ZooKeeperInitException("Unable to connect to the zookeeper server.");
        }
    }

    /**
     * Wait connection
     */
    private void waitConnect() {
        int tryNum = 0;

        // Block the zookeeper connection process to prevent the connection state from causing the plugin service
        // initialization failure that depends on the service
        while (zkClient.getState() == ZooKeeper.States.CONNECTING && tryNum++ <= CONFIG.getConnectRetryTimes()) {
            try {
                Thread.sleep(CONFIG.getConnectTimeout());
            } catch (InterruptedException e) {
                // ignored
            }
        }
    }

    /**
     * Check whether the node exists
     *
     * @param path node path
     * @return whether the node exists
     */
    public boolean ifNodeExist(String path) {
        return zkClient.ifNodeExist(path);
    }

    /**
     * Get configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @return node content
     */
    public String getNode(String key, String group) {
        return zkClient.getConfig(key, group);
    }

    /**
     * Create the parent node of a node
     *
     * @param path node path
     * @return create result
     */
    public boolean createParent(String path) {
        return zkClient.createParent(path);
    }

    /**
     * Update content of node, which is automatically created when it does not exist
     *
     * @param key node name
     * @param group node parent path
     * @param data data
     * @return update result
     */
    public boolean updateNode(String key, String group, String data) {
        return zkClient.publishConfig(key, group, data);
    }

    /**
     * Remove node
     *
     * @param key node name
     * @param group node parent path
     * @return remove result
     */
    public boolean removeNode(String key, String group) {
        return zkClient.removeConfig(key, group);
    }

    /**
     * Query the path list of all descendant nodes under a node
     *
     * @param path node path
     * @return path list
     */
    public List<String> listAllNodes(String path) {
        return zkClient.listAllNodes(path);
    }

    /**
     * Adds a loop watch for temporary data that will re-register after triggering until the listener removal event is
     * received
     * <p>
     * Note that when other listeners on the same node are precisely removed, the watcher will choose to abandon the
     * loop registration because it cannot identify whether to remove itself
     *
     * @param path node path
     * @param watcher The actual executing watcher
     * @param handler Exception handler after loop registration failure of watcher
     * @return add result
     */
    public boolean addDataLoopWatch(String path, Watcher watcher, ZooKeeperClient.BreakHandler handler) {
        return zkClient.addDataLoopWatch(path, watcher, handler);
    }

    /**
     * Adds persistent recursive listeners, effective for descendant nodes
     *
     * @param path node path
     * @param watcher node watcher
     * @return add result
     */
    public boolean addPersistentRecursiveWatches(String path, Watcher watcher) {
        return zkClient.addPersistentRecursiveWatches(path, watcher);
    }

    /**
     * Remove data watchers
     *
     * @param path node path
     * @return remove result
     */
    public boolean removeDataWatches(String path) {
        return zkClient.removeDataWatches(path);
    }

    /**
     * Remove all watchers from nodes, including children nodes
     *
     * @param path node path
     * @return remove result
     */
    public boolean removeAllWatches(String path) {
        return zkClient.removeAllWatches(path);
    }

    @Override
    public void close() {
        try {
            zkClient.close();
        } catch (InterruptedException ignored) {
            LOGGER.warning("Unexpected exception occurs. ");
        }
    }
}
