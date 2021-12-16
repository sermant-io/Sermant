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

package com.huawei.sermant.core.service.dynamicconfig.zookeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigChangeEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigChangeType;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

/**
 * 动态配置服务，zookeeper实现
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/12/15
 */
public class ZooKeeperDynamicConfigService extends DynamicConfigService {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * zookeeper的包装客户端
     */
    private ZooKeeperBufferedClient zkClient;

    @Override
    public void start() {
        zkClient = new ZooKeeperBufferedClient(CONFIG.getServerAddress(), CONFIG.getTimeoutValue());
    }

    @Override
    public void stop() {
        zkClient.close();
    }

    /**
     * 修正zk路径，补充缺少的斜杠
     *
     * @param path zk节点路径
     * @return 修正后的路径
     */
    private String fixPath(String path) {
        return path == null || path.length() <= 0 ? "" :
                (path.startsWith(ZooKeeperBufferedClient.ZK_SEPARATOR) ? path :
                        ZooKeeperBufferedClient.ZK_SEPARATOR + path);
    }

    /**
     * 修正组，如果组为空，则使用默认的组替代
     *
     * @param group 组
     * @return 修正后的组
     */
    private String fixGroup(String group) {
        return group == null || group.length() <= 0 ? CONFIG.getDefaultGroup() : group;
    }

    @Override
    public String getConfig(String key, String group) {
        if (key == null || key.length() <= 0) {
            LOGGER.warning("Empty key is not allowed. ");
            return null;
        }
        if (group == null) {
            return getConfig(key);
        }
        return zkClient.getNode(fixPath(fixGroup(group)) + fixPath(key));
    }

    @Override
    public boolean publishConfig(String key, String group, String content) {
        if (key == null || key.length() <= 0) {
            LOGGER.warning("Empty key is not allowed. ");
            return false;
        }
        if (group == null) {
            return publishConfig(key, content);
        }
        return zkClient.updateNode(fixPath(fixGroup(group)) + fixPath(key), content);
    }

    @Override
    public boolean removeConfig(String key, String group) {
        if (key == null || key.length() <= 0) {
            LOGGER.warning("Empty key is not allowed. ");
            return false;
        }
        if (group == null) {
            return removeConfig(key);
        }
        return zkClient.removeNode(fixPath(fixGroup(group)) + fixPath(key));
    }

    /**
     * 转换zk事件类型
     *
     * @param type zk事件类型
     * @return 转换后的动态配置改变类型
     */
    private DynamicConfigChangeType transEventType(Watcher.Event.EventType type) {
        switch (type) {
            case NodeCreated:
                return DynamicConfigChangeType.ADDED;
            case NodeDeleted:
                return DynamicConfigChangeType.DELETED;
            case None:
            case NodeDataChanged:
            case DataWatchRemoved:
            case ChildWatchRemoved:
            case NodeChildrenChanged:
            case PersistentWatchRemoved:
            default:
                return DynamicConfigChangeType.MODIFIED;
        }
    }

    @Override
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener) {
        if (key == null || key.length() <= 0) {
            LOGGER.warning("Empty key is not allowed. ");
            return false;
        }
        if (group == null) {
            return addConfigListener(key, listener);
        }
        final String fixedGroup = fixGroup(group);
        final String fullPath = fixPath(fixedGroup) + fixPath(key);
        return zkClient.addDataLoopWatch(fullPath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if (!fullPath.equals(watchedEvent.getPath())) {
                    LOGGER.warning(String.format(Locale.ROOT,
                            "Unexpected event path, giving [%s], but expecting [%s]. ",
                            watchedEvent.getPath(), fullPath));
                    return;
                }
                final DynamicConfigChangeType changeType = transEventType(watchedEvent.getType());
                final String content =
                        changeType == DynamicConfigChangeType.DELETED ? "" : getConfig(key, fixedGroup);
                listener.process(new DynamicConfigChangeEvent(key, fixedGroup, content, changeType));
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
    public boolean addGroupListener(String group, DynamicConfigListener listener) {
        final String fixedGroup = fixPath(fixGroup(group));
        return zkClient.addPersistentRecursiveWatches(fixedGroup, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                final String eventPath = watchedEvent.getPath();
                if (eventPath == null) {
                    LOGGER.warning("Unexpected empty event path. ");
                    return;
                }
                if (fixedGroup.equals(eventPath)) {
                    LOGGER.fine(String.format(Locale.ROOT, "Skip processing group event [%s]. ", fixedGroup));
                    return;
                }
                if (!eventPath.startsWith(fixedGroup)) {
                    LOGGER.warning(String.format(Locale.ROOT,
                            "Event path [%s] is not child of [%s]. ", eventPath, fixedGroup));
                    return;
                }
                final DynamicConfigChangeType changeType = transEventType(watchedEvent.getType());
                final String key = eventPath.substring(fixedGroup.length());
                final String content =
                        changeType == DynamicConfigChangeType.DELETED ? "" : getConfig(key, fixedGroup);
                listener.process(new DynamicConfigChangeEvent(key, group, content, changeType));
            }
        });
    }

    @Override
    public boolean removeConfigListener(String key, String group) {
        if (key == null || key.length() <= 0) {
            LOGGER.warning("Empty key is not allowed. ");
            return false;
        }
        if (group == null) {
            return removeConfig(key);
        }
        return zkClient.removeDataWatches(fixPath(fixGroup(group)) + fixPath(key));
    }

    @Override
    public boolean removeGroupListener(String group) {
        return zkClient.removeAllWatches(fixPath(fixGroup(group)));
    }

    @Override
    public List<String> listKeysFromGroup(String group) {
        final String fixedGroup = fixPath(fixGroup(group));
        final List<String> keys = new ArrayList<>();
        for (String path : zkClient.listAllNodes(fixedGroup)) {
            if (path.startsWith(fixedGroup)) {
                keys.add(path.substring(fixedGroup.length()));
            }
        }
        return keys;
    }
}
