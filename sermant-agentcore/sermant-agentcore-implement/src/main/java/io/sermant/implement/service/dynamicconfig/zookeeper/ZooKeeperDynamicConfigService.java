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

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.dynamicconfig.DynamicConfigService;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Dynamic configuration service, ZooKeeper implementation
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-15
 */
public class ZooKeeperDynamicConfigService extends DynamicConfigService {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * ZK path separator
     */
    private static final char ZK_PATH_SEPARATOR = ZooKeeperBufferedClient.ZK_PATH_SEPARATOR;

    /**
     * ZooKeeper buffered client
     */
    private ZooKeeperBufferedClient zkClient;

    @Override
    public void start() {
        if (CONFIG.isEnableAuth()) {
            zkClient = new ZooKeeperBufferedClient(CONFIG.getServerAddress(), CONFIG.getTimeoutValue(),
                    CONFIG.getUserName(), CONFIG.getPassword(), CONFIG.getPrivateKey());
        } else {
            zkClient = new ZooKeeperBufferedClient(CONFIG.getServerAddress(), CONFIG.getTimeoutValue());
        }
    }

    @Override
    public void stop() {
        zkClient.close();
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
     * Get the ZK path and fills in the missing "/"
     *
     * @param key key
     * @param group group
     * @return ZK path
     */
    private String toPath(String key, String group) {
        return toPath(group) + toPath(key);
    }

    /**
     * Convert ZK events to dynamic configuration events
     *
     * @param key configuration key
     * @param group configuration group
     * @param watchedEvent ZK event
     * @return Dynamic configuration event
     */
    private DynamicConfigEvent transEvent(String key, String group, WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case NodeCreated:
                return DynamicConfigEvent.createEvent(key, group, doGetConfig(key, group).orElse(null));
            case NodeDeleted:
                return DynamicConfigEvent.deleteEvent(key, group, doGetConfig(key, group).orElse(null));
            case None:
            case NodeDataChanged:
            case DataWatchRemoved:
            case ChildWatchRemoved:
            case NodeChildrenChanged:
            case PersistentWatchRemoved:
            default:
                return DynamicConfigEvent.modifyEvent(key, group, doGetConfig(key, group).orElse(null));
        }
    }

    @Override
    public Optional<String> doGetConfig(String key, String group) {
        return Optional.ofNullable(zkClient.getNode(toPath(key, group)));
    }

    @Override
    public boolean doPublishConfig(String key, String group, String content) {
        return zkClient.updateNode(toPath(key, group), content);
    }

    @Override
    public boolean doRemoveConfig(String key, String group) {
        return zkClient.removeNode(toPath(key, group));
    }

    @Override
    public boolean doAddConfigListener(String key, String group, DynamicConfigListener listener) {
        final String fullPath = toPath(key, group);
        return zkClient.addDataLoopWatch(fullPath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if (!fullPath.equals(watchedEvent.getPath())) {
                    LOGGER.warning(String.format(Locale.ROOT,
                            "Unexpected event path, giving [%s], but expecting [%s]. ",
                            watchedEvent.getPath(), fullPath));
                    return;
                }
                listener.process(transEvent(key, group, watchedEvent));
            }
        }, new ZooKeeperBufferedClient.BreakHandler() {
            @Override
            public void handle(Throwable throwable) {
                LOGGER.warning(String.format(Locale.ROOT,
                        "Cancel watch [%s] for [%s]: [%s]. ", fullPath, throwable.getClass(), throwable.getMessage()));
            }
        });
    }

    @Override
    public boolean doRemoveConfigListener(String key, String group) {
        return zkClient.removeDataWatches(toPath(key, group));
    }

    @Override
    public List<String> doListKeysFromGroup(String group) {
        final String groupPath = toPath(group);
        final List<String> keys = new ArrayList<>();
        for (String keyPath : zkClient.listAllNodes(groupPath)) {
            if (keyPath.startsWith(groupPath)) {
                keys.add(keyPath.substring(groupPath.length() + 1));
            }
        }
        return keys;
    }

    @Override
    public boolean doAddGroupListener(String group, DynamicConfigListener listener) {
        final String groupPath = toPath(group);
        return zkClient.addPersistentRecursiveWatches(groupPath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                final String eventPath = watchedEvent.getPath();
                if (eventPath == null) {
                    LOGGER.warning("Unexpected empty event path. ");
                    return;
                }
                if (groupPath.equals(eventPath)) {
                    LOGGER.fine(String.format(Locale.ROOT, "Skip processing group event [%s]. ", groupPath));
                    return;
                }
                if (!eventPath.startsWith(groupPath) || eventPath.charAt(groupPath.length()) != ZK_PATH_SEPARATOR) {
                    LOGGER.warning(String.format(Locale.ROOT,
                            "Event path [%s] is not child of [%s]. ", eventPath, groupPath));
                    return;
                }
                listener.process(transEvent(eventPath.substring(groupPath.length() + 1), group, watchedEvent));
            }
        });
    }

    @Override
    public boolean doRemoveGroupListener(String group) {
        return zkClient.removeAllWatches(toPath(group));
    }
}
