/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

import io.sermant.implement.service.dynamicconfig.ConfigClient;

import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link ZooKeeper} wrapper, which wraps the ZooKeeper native apis and provides easier apis to use
 *
 * @author zhp
 * @since 2024-05-15
 */
public class ZooKeeperClient implements ConfigClient {
    /**
     * ZK path separator
     */
    public static final char ZK_PATH_SEPARATOR = '/';
    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperClient.class.getName());

    /**
     * ZK client
     */
    private final ZooKeeper zkClient;

    /**
     * Create a ZooKeeperClient, initialize the ZK client
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @param watcher Watcher for event processing
     * @throws ZooKeeperInitException zk initialization exception
     */
    public ZooKeeperClient(String connectString, int sessionTimeout, Watcher watcher) {
        try {
            zkClient = new ZooKeeper(connectString, sessionTimeout, watcher);
        } catch (IOException ignored) {
            throw new ZooKeeperInitException("Connect to " + connectString + "failed. ");
        }
    }

    /**
     * Add the specified scheme:auth information to this connection.
     *
     * @param scheme Permission control scheme
     * @param auth Authorization information
     */
    public void addAuthInfo(String scheme, byte[] auth) {
        zkClient.addAuthInfo(scheme, auth);
    }

    /**
     * get the connection state
     *
     * @return connection state
     */
    public ZooKeeper.States getState() {
        return zkClient.getState();
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

    @Override
    public String getConfig(String key, String group) {
        String path = toPath(key, group);
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
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public Map<String, List<String>> getConfigList(String key, String group, boolean flag) {
        String path = toPath(group);
        try {
            if (!flag) {
                return fuzzyGetConfigListByGroupAndKey(key, path);
            }
            if (key == null || key.isEmpty()) {
                return accurateGetConfigListByGroup(path);
            }
            return accurateGetConfigListByGroupAndKey(key, path);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("Exception in querying configuration list", e);
        }
        return Collections.EMPTY_MAP;
    }

    private Map<String, List<String>> fuzzyGetConfigListByGroupAndKey(String key, String path)
            throws KeeperException, InterruptedException {
        Map<String, List<String>> configList = new HashMap<>();
        int index = path.lastIndexOf(ZK_PATH_SEPARATOR);
        String parentNodePath;
        if (index == 0) {
            parentNodePath = path.substring(0, index + 1);
        } else {
            parentNodePath = path.substring(0, index);
        }
        String nodeName = path.substring(index + 1);
        if (!ifNodeExist(parentNodePath)) {
            return configList;
        }
        List<String> childList = this.zkClient.getChildren(parentNodePath, false);
        for (String child : childList) {
            if (!child.contains(nodeName)) {
                continue;
            }
            String childPath;
            if (parentNodePath.endsWith(String.valueOf(ZK_PATH_SEPARATOR))) {
                childPath = parentNodePath + child;
            } else {
                childPath = parentNodePath + ZK_PATH_SEPARATOR + child;
            }
            List<String> subChild = this.zkClient.getChildren(childPath, false);
            if (subChild == null || subChild.isEmpty()) {
                continue;
            }
            if (key == null || key.isEmpty()) {
                configList.put(childPath.substring(1), subChild);
                continue;
            }
            List<String> matchSubChild = subChild.stream().filter(value -> value.contains(key))
                    .collect(Collectors.toList());
            if (matchSubChild.isEmpty()) {
                continue;
            }
            configList.put(childPath.substring(1), matchSubChild);
        }
        return configList;
    }

    private Map<String, List<String>> accurateGetConfigListByGroupAndKey(String key, String group)
            throws KeeperException, InterruptedException {
        Map<String, List<String>> configList = new HashMap<>();
        if (!ifNodeExist(group)) {
            return configList;
        }
        List<String> childList = this.zkClient.getChildren(group, false);
        configList.put(group.substring(1), childList.stream()
                .filter(value -> value.equals(key))
                .collect(Collectors.toList()));
        return configList;
    }

    private Map<String, List<String>> accurateGetConfigListByGroup(String group)
            throws KeeperException, InterruptedException {
        Map<String, List<String>> configList = new HashMap<>();
        if (!ifNodeExist(group)) {
            return configList;
        }
        List<String> childList = this.zkClient.getChildren(group, false);
        configList.put(group.substring(1), childList);
        return configList;
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

    @Override
    public boolean publishConfig(String key, String group, String data) {
        try {
            String path = toPath(key, group);
            if (ifNodeExist(path)) {
                getZkClient().setData(path, data.getBytes(StandardCharsets.UTF_8), -1);
            } else {
                if (!createParent(path)) {
                    return false;
                }
                getZkClient().create(path, data.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException ignored) {
            return false;
        }
        return true;
    }

    @Override
    public boolean removeConfig(String key, String group) {
        String path = toPath(key, group);
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

    @Override
    public boolean isConnect() {
        return zkClient.getState() == ZooKeeper.States.CONNECTED
                || zkClient.getState() == ZooKeeper.States.CONNECTEDREADONLY;
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

    /**
     * close zookeeper client
     *
     * @throws InterruptedException An exception occurred when closing the Zookeeper client
     */
    public void close() throws InterruptedException {
        zkClient.close();
    }

    /**
     * Get the ZK path and fills in the missing "/"
     *
     * @param key key
     * @param group group
     * @return ZK path
     */
    private String toPath(String key, String group) {
        if (group == null || group.isEmpty() || String.valueOf(ZK_PATH_SEPARATOR).equals(group)) {
            return toPath(key);
        }
        if (key == null || key.isEmpty()) {
            return toPath(group);
        }
        return toPath(group) + toPath(key);
    }

    /**
     * Get the ZK path and fills in the missing "/"
     *
     * @param keyOrGroup key or group
     * @return ZK path
     */
    private String toPath(String keyOrGroup) {
        return keyOrGroup.charAt(0) == ZK_PATH_SEPARATOR ? keyOrGroup : ZK_PATH_SEPARATOR + keyOrGroup;
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
