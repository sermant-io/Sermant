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
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

/**
 * 动态配置服务，zookeeper实现
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-15
 */
public class ZooKeeperDynamicConfigService extends DynamicConfigService {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * zk路径分隔符
     */
    private static final char ZK_PATH_SEPARATOR = ZooKeeperBufferedClient.ZK_PATH_SEPARATOR;

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
     * 获取zk路径，补充缺少的斜杠
     *
     * @param keyOrGroup key或group
     * @return zk路径
     */
    private String toPath(String keyOrGroup) {
        return keyOrGroup.charAt(0) == ZK_PATH_SEPARATOR ? keyOrGroup : ZK_PATH_SEPARATOR + keyOrGroup;
    }

    /**
     * 获取zk路径，补充缺少的斜杠
     *
     * @param key   键
     * @param group 组
     * @return zk路径
     */
    private String toPath(String key, String group) {
        return toPath(group) + toPath(key);
    }

    /**
     * 将zk事件转换为动态配置事件
     *
     * @param key          配置键
     * @param group        分组
     * @param watchedEvent zk事件
     * @return 动态配置事件
     */
    private DynamicConfigEvent transEvent(String key, String group, WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case NodeCreated:
                return DynamicConfigEvent.createEvent(key, group, doGetConfig(key, group));
            case NodeDeleted:
                return DynamicConfigEvent.deleteEvent(key, group, doGetConfig(key, group));
            case None:
            case NodeDataChanged:
            case DataWatchRemoved:
            case ChildWatchRemoved:
            case NodeChildrenChanged:
            case PersistentWatchRemoved:
            default:
                return DynamicConfigEvent.modifyEvent(key, group, doGetConfig(key, group));
        }
    }

    @Override
    protected String doGetConfig(String key, String group) {
        return zkClient.getNode(toPath(key, group));
    }

    @Override
    protected boolean doPublishConfig(String key, String group, String content) {
        return zkClient.updateNode(toPath(key, group), content);
    }

    @Override
    protected boolean doRemoveConfig(String key, String group) {
        return zkClient.removeNode(toPath(key, group));
    }

    @Override
    protected boolean doAddConfigListener(String key, String group, DynamicConfigListener listener) {
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
    protected boolean doRemoveConfigListener(String key, String group) {
        return zkClient.removeDataWatches(toPath(key, group));
    }

    @Override
    protected List<String> doListKeysFromGroup(String group) {
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
    protected boolean doAddGroupListener(String group, DynamicConfigListener listener) {
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
    protected boolean doRemoveGroupListener(String group) {
        return zkClient.removeAllWatches(toPath(group));
    }
}
