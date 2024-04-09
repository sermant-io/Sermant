/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.implement.service.dynamicconfig.zookeeper;

import com.huaweicloud.sermant.core.common.CommonConstant;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.notification.NotificationInfo;
import com.huaweicloud.sermant.core.notification.NotificationManager;
import com.huaweicloud.sermant.core.notification.ZookeeperNotificationType;
import com.huaweicloud.sermant.core.service.dynamicconfig.config.DynamicConfig;
import com.huaweicloud.sermant.core.utils.AesUtil;

import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    private ZooKeeper zkClient;

    /**
     * Create a ZooKeeperBufferedClient, initialize the ZK client, and provide an expired reconnection mechanism
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @throws ZooKeeperInitException In the case of dependent dynamic configuration, if ZK initialization fails then
     * Sermant needs to be interrupted
     */
    public ZooKeeperBufferedClient(String connectString, int sessionTimeout) {
        zkClient = newZkClient(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                // 连接过期重连
                if (event.getState() == Event.KeeperState.Expired) {
                    zkClient = newZkClient(connectString, sessionTimeout, this);
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
        zkClient = newZkClient(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                // The connection expires and reconnects
                if (event.getState() == Event.KeeperState.Expired) {
                    zkClient = newZkClient(connectString, sessionTimeout, this);
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
     * Create ZK client
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @param watcher default watcher
     * @return ZK client
     * @throws ZooKeeperInitException zk initialization exception
     */
    private ZooKeeper newZkClient(String connectString, int sessionTimeout, Watcher watcher) {
        try {
            return new ZooKeeper(connectString, sessionTimeout, watcher);
        } catch (IOException ignored) {
            throw new ZooKeeperInitException("Connect to " + connectString + "failed. ");
        }
    }

    /**
     * Gets the ZK client and throws an exception if the client is disconnected
     *
     * @return ZK client
     * @throws ZooKeeperConnectionException zk initialization exception
     */
    private ZooKeeper getZkClient() {
        final ZooKeeper.States state = zkClient.getState();
        if (state == ZooKeeper.States.CONNECTED || state == ZooKeeper.States.CONNECTEDREADONLY) {
            return zkClient;
        }
        throw new ZooKeeperConnectionException("Unable to connect to the zookeeper server, connection timeout.");
    }

    /**
     * Check whether the node exists
     *
     * @param path node path
     * @return whether the node exists
     */
    public boolean ifNodeExist(String path) {
        try {
            return zkClient.exists(path, false) != null;
        } catch (KeeperException | InterruptedException ignored) {
            return false;
        }
    }

    /**
     * Query node content
     *
     * @param path node path
     * @return node content
     */
    public String getNode(String path) {
        if (!ifNodeExist(path)) {
            return "";
        }
        final byte[] data;
        try {
            data = getZkClient().getData(path, false, null);
        } catch (KeeperException | InterruptedException ignored) {
            return "";
        }
        if (data == null) {
            return "";
        }
        return new String(data, CommonConstant.DEFAULT_CHARSET);
    }

    /**
     * Create the parent node of a node
     *
     * @param path node path
     * @return create result
     */
    public boolean createParent(String path) {
        final int separatorIndex = path.lastIndexOf(ZK_PATH_SEPARATOR);
        if (separatorIndex == 0) {
            return true;
        }
        final String parent = path.substring(0, separatorIndex);
        if (ifNodeExist(parent)) {
            return true;
        }
        if (!createParent(parent)) {
            return false;
        }
        try {
            getZkClient().create(parent, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException | InterruptedException ignored) {
            return false;
        }
        return true;
    }

    /**
     * Update content of node, which is automatically created when it does not exist
     *
     * @param path node path
     * @param data data
     * @return update result
     */
    public boolean updateNode(String path, String data) {
        try {
            if (ifNodeExist(path)) {
                getZkClient().setData(path, data.getBytes(CommonConstant.DEFAULT_CHARSET), -1);
            } else {
                if (!createParent(path)) {
                    return false;
                }
                getZkClient().create(path, data.getBytes(CommonConstant.DEFAULT_CHARSET), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException ignored) {
            return false;
        }
        return true;
    }

    /**
     * Remove node
     *
     * @param path node path
     * @return remove result
     */
    public boolean removeNode(String path) {
        if (!ifNodeExist(path)) {
            return true;
        }
        try {
            getZkClient().delete(path, -1);
        } catch (InterruptedException | KeeperException ignored) {
            return false;
        }
        return true;
    }

    /**
     * Query the path list of all descendant nodes under a node
     *
     * @param path node path
     * @return path list
     */
    public List<String> listAllNodes(String path) {
        if (!ifNodeExist(path)) {
            return Collections.emptyList();
        }
        final List<String> children;
        try {
            children = getZkClient().getChildren(path, false);
        } catch (KeeperException | InterruptedException ignored) {
            return Collections.emptyList();
        }
        final List<String> nodes = new ArrayList<>();
        for (String child : children) {
            final String childPath = path + ZK_PATH_SEPARATOR + child;
            nodes.add(childPath);
            nodes.addAll(listAllNodes(childPath));
        }
        return nodes;
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
    public boolean addDataLoopWatch(String path, Watcher watcher, BreakHandler handler) {
        final Watcher bufferedWatcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                final Event.EventType type = event.getType();
                if (type == Event.EventType.DataWatchRemoved) {
                    return;
                }
                watcher.process(event);
                try {
                    getZkClient().exists(path, this);
                } catch (KeeperException | InterruptedException e) {
                    handler.handle(e);
                }
            }
        };
        try {
            getZkClient().exists(path, bufferedWatcher);
        } catch (KeeperException | InterruptedException ignored) {
            return false;
        }
        return true;
    }

    /**
     * Adds persistent recursive listeners, effective for descendant nodes
     *
     * @param path node path
     * @param watcher node watcher
     * @return add result
     */
    public boolean addPersistentRecursiveWatches(String path, Watcher watcher) {
        try {
            getZkClient().addWatch(path, watcher, AddWatchMode.PERSISTENT_RECURSIVE);
        } catch (KeeperException | InterruptedException ignored) {
            return false;
        }
        return true;
    }

    /**
     * Remove data watchers
     *
     * @param path node path
     * @return remove result
     */
    public boolean removeDataWatches(String path) {
        try {
            getZkClient().removeAllWatches(path, Watcher.WatcherType.Data, false);
        } catch (KeeperException | InterruptedException ignored) {
            return false;
        }
        return true;
    }

    /**
     * Remove all watchers from nodes, including children nodes
     *
     * @param path node path
     * @return remove result
     */
    public boolean removeAllWatches(String path) {
        try {
            getZkClient().removeAllWatches(path, Watcher.WatcherType.Any, false);
        } catch (KeeperException | InterruptedException ignored) {
            return false;
        }
        return true;
    }

    @Override
    public void close() {
        try {
            zkClient.close();
        } catch (InterruptedException ignored) {
            LOGGER.warning("Unexpected exception occurs. ");
        }
    }

    /**
     * The loop out processor is currently used to handle cases where loop registration is accidentally terminated
     *
     * @since 2021-12-15
     */
    public interface BreakHandler {
        /**
         * Handles cases where circular registration is accidentally terminated
         *
         * @param throwable throwable
         */
        void handle(Throwable throwable);
    }
}
