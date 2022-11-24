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

package com.huaweicloud.sermant.implement.service.dynamicconfig.kie.client.kie;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.constants.KieConstants;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.listener.KvDataHolder;
import com.huaweicloud.sermant.implement.service.dynamicconfig.kie.listener.SubscriberManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * listener封装，关联任务执行器
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KieListenerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final String group;

    private final KvDataHolder kvDataHolder;

    private final KieRequest kieRequest;

    private long currentVersion;

    /**
     * key : 键 VersionListenerWrapper: 单个key监听者集合 单个key映射多个listener， 针对单个key的场景进行监听
     */
    private final Map<String, VersionListenerWrapper> keyListenerMap = new HashMap<>();

    private SubscriberManager.Task task;

    /**
     * 包装构建器
     *
     * @param key 键
     * @param dynamicConfigListener 监听器
     * @param kvDataHolder 数据持有器
     * @param kieRequest 请求
     * @param ifNotify 是否需要初始化通知
     */
    public KieListenerWrapper(String key, DynamicConfigListener dynamicConfigListener,
            KvDataHolder kvDataHolder, KieRequest kieRequest, boolean ifNotify) {
        this.group = kieRequest.getLabelCondition();
        this.kvDataHolder = kvDataHolder;
        this.kieRequest = kieRequest;
        addKeyListener(key, dynamicConfigListener, ifNotify);
    }

    /**
     * 通知监听器
     *
     * @param eventDataHolder 改事件的数据持有
     * @param isFirst 是否为第一次通知
     */
    public void notifyListeners(KvDataHolder.EventDataHolder eventDataHolder, boolean isFirst) {
        currentVersion = eventDataHolder.getVersion();
        if (!eventDataHolder.getAdded().isEmpty()) {
            // 新增事件
            notifyAdded(eventDataHolder.getAdded(), eventDataHolder.getLatestData(), isFirst);
        }
        if (!eventDataHolder.getDeleted().isEmpty()) {
            // 删除事件
            notify(eventDataHolder.getDeleted(), DynamicConfigEventType.DELETE, null);
        }
        if (!eventDataHolder.getModified().isEmpty()) {
            // 修改事件
            notify(eventDataHolder.getModified(), DynamicConfigEventType.MODIFY, null);
        }
    }

    private void notifyAdded(Map<String, String> addedData, Map<String, String> latestData, boolean isFirst) {
        if (!isFirst) {
            notify(addedData, DynamicConfigEventType.CREATE, null);
            return;
        }
        notify(addedData, DynamicConfigEventType.CREATE, latestData);
    }

    private void notify(Map<String, String> configData, DynamicConfigEventType dynamicConfigEventType,
            Map<String, String> latestData) {
        for (Map.Entry<String, String> entry : configData.entrySet()) {
            // 通知单个key监听器
            notifyEvent(entry.getKey(), entry.getValue(), dynamicConfigEventType, false, latestData);

            // 通知该Group的监听器做更新
            notifyEvent(entry.getKey(), entry.getValue(), dynamicConfigEventType, true, latestData);
        }
    }

    private void notifyEvent(String key, String value, DynamicConfigEventType eventType, boolean isGroup,
            Map<String, String> latestData) {
        final VersionListenerWrapper versionListenerWrapper = keyListenerMap
                .get(isGroup ? KieConstants.DEFAULT_GROUP_KEY : key);
        if (versionListenerWrapper == null) {
            return;
        }
        notifyEvent(key, value, eventType, versionListenerWrapper, latestData);
    }

    private void notifyEvent(String key, String value, DynamicConfigEventType eventType, VersionListenerWrapper wrapper,
            Map<String, String> latestData) {
        DynamicConfigEvent event;
        switch (eventType) {
            case INIT:
                event = DynamicConfigEvent.initEvent(key, this.group, value);
                break;
            case CREATE:
                event = DynamicConfigEvent.createEvent(key, this.group, value);
                break;
            case MODIFY:
                event = DynamicConfigEvent.modifyEvent(key, this.group, value);
                break;
            case DELETE:
                event = DynamicConfigEvent.deleteEvent(key, this.group, value);
                break;
            default:
                LOGGER.warning(String.format(Locale.ENGLISH, "Event type [%s] is invalid. ", eventType));
                return;
        }
        processAllListeners(event, wrapper, latestData);
    }

    private void processAllListeners(DynamicConfigEvent event, VersionListenerWrapper versionListenerWrapper,
            Map<String, String> latestData) {
        if (versionListenerWrapper.listeners == null) {
            return;
        }
        for (VersionListener versionListener : versionListenerWrapper.listeners) {
            try {
                if (versionListener.version > currentVersion) {
                    // 已通知的listener不再通知, 避免针对同一个group的多个不同key进行多次重复通知
                    continue;
                }
                if (event.getEventType() == DynamicConfigEventType.INIT) {
                    processInit(latestData, versionListener, event);
                } else {
                    versionListener.listener.process(event);
                }
                versionListener.version = currentVersion;
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, String.format(Locale.ENGLISH,
                        "Process config data failed, key: [%s], group: [%s]",
                        event.getKey(), this.group), ex);
            }
        }
    }

    private void processInit(Map<String, String> latestData, VersionListener versionListener,
            DynamicConfigEvent event) {
        if (versionListener.isNeedInit && !versionListener.isInitializer && latestData != null) {
            // 需要初始化, 但是未初始化, 全量数据通知
            for (Map.Entry<String, String> entry : latestData.entrySet()) {
                versionListener.listener
                        .process(DynamicConfigEvent.initEvent(entry.getKey(), event.getGroup(), entry.getValue()));
            }
            versionListener.isInitializer = true;
            versionListener.initVersion = currentVersion;
        } else {
            if (versionListener.initVersion != 0 && versionListener.initVersion != currentVersion) {
                // 其他已经通知过或者不需要通知的采用add事件, 通知前判断其初始化通知的版本是否为当前版本, 若为当前版本则表示已经全量通知过, 无需再次通知
                versionListener.listener.process(DynamicConfigEvent.createEvent(event.getKey(), event.getGroup(),
                        event.getContent()));
            }
        }
    }

    /**
     * 添加key监听器
     *
     * @param key 键
     * @param dynamicConfigListener 监听器
     * @param ifNotify 是否初始化通知
     */
    public void addKeyListener(String key, DynamicConfigListener dynamicConfigListener, boolean ifNotify) {
        VersionListenerWrapper versionListenerWrapper = keyListenerMap.get(key);
        if (versionListenerWrapper == null) {
            versionListenerWrapper = new VersionListenerWrapper();
        }
        versionListenerWrapper.addListener(dynamicConfigListener, ifNotify);
        keyListenerMap.put(key, versionListenerWrapper);
    }

    /**
     * 移除key监听器
     *
     * @param key 键
     * @param listener 监听器
     * @return 是否移除成功
     */
    public boolean removeKeyListener(String key, DynamicConfigListener listener) {
        final VersionListenerWrapper versionListenerWrapper = keyListenerMap.get(key);
        if (versionListenerWrapper == null) {
            return false;
        }
        return versionListenerWrapper.removeListener(listener);
    }

    /**
     * 当前分组的所有监听器是否全部清空
     *
     * @return 为空 返回 true
     */
    public boolean isEmpty() {
        for (VersionListenerWrapper wrap : keyListenerMap.values()) {
            if (!wrap.listeners.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public SubscriberManager.Task getTask() {
        return task;
    }

    public void setTask(SubscriberManager.Task task) {
        this.task = task;
    }

    public KvDataHolder getKvDataHolder() {
        return kvDataHolder;
    }

    public KieRequest getKieRequest() {
        return kieRequest;
    }

    /**
     * listeners包装器
     *
     * @since 2022-01-01
     */
    static class VersionListenerWrapper {
        Set<VersionListener> listeners;

        VersionListenerWrapper() {
            listeners = new HashSet<>();
        }

        void addListener(DynamicConfigListener listener, boolean ifNotify) {
            listeners.add(new VersionListener(-1L, listener, ifNotify));
        }

        boolean removeListener(DynamicConfigListener listener) {
            // VersionListener基于listener移除
            return listeners.remove(new VersionListener(-1L, listener, false));
        }
    }

    /**
     * listener包装器, 增加版本号判断
     *
     * @since 2022-01-01
     */
    static class VersionListener {
        DynamicConfigListener listener;

        /**
         * 已通知的版本号, 基于KIE数据版本号，即revision
         */
        long version;

        /**
         * 是否被INIT事件通知, 该事件仅通知一次
         */
        boolean isInitializer = false;

        /**
         * 是否需要首次通知
         */
        boolean isNeedInit;

        /**
         * 被初始化的版本
         */
        long initVersion;

        VersionListener(long version, DynamicConfigListener listener, boolean isNeedInit) {
            this.listener = listener;
            this.version = version;
            this.isNeedInit = isNeedInit;
        }

        @Override
        public boolean equals(Object target) {
            if (this == target) {
                return true;
            }
            if (target == null || getClass() != target.getClass()) {
                return false;
            }
            VersionListener that = (VersionListener) target;

            return listener != null ? listener.equals(that.listener) : that.listener == null;
        }

        @Override
        public int hashCode() {
            return listener != null ? listener.hashCode() : 0;
        }
    }
}
