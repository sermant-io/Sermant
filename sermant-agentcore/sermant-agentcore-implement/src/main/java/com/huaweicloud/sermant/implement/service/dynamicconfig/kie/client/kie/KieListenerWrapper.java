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
 * KieListenerWrapper，associated the task executor
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
     * key : String, value : VersionListenerWrapper, collection of single key listeners. A single key maps multiple
     * listeners. Listen to scenarios with a single key
     */
    private final Map<String, VersionListenerWrapper> keyListenerMap = new HashMap<>();

    private SubscriberManager.Task task;

    /**
     * Constructor
     *
     * @param key key
     * @param dynamicConfigListener dynamic config listener
     * @param kvDataHolder data holder
     * @param kieRequest kie request
     * @param ifNotify if notify
     */
    public KieListenerWrapper(String key, DynamicConfigListener dynamicConfigListener,
            KvDataHolder kvDataHolder, KieRequest kieRequest, boolean ifNotify) {
        this.group = kieRequest.getLabelCondition();
        this.kvDataHolder = kvDataHolder;
        this.kieRequest = kieRequest;
        this.addKeyListener(key, dynamicConfigListener, ifNotify);
    }

    /**
     * Notify listeners
     *
     * @param eventDataHolder data holder
     * @param isFirst Whether it is the first notification
     */
    public void notifyListeners(KvDataHolder.EventDataHolder eventDataHolder, boolean isFirst) {
        currentVersion = eventDataHolder.getVersion();
        if (!eventDataHolder.getAdded().isEmpty()) {
            // Create event
            notifyAdded(eventDataHolder.getAdded(), eventDataHolder.getLatestData(), isFirst);
        }
        if (!eventDataHolder.getDeleted().isEmpty()) {
            // Delete event
            notify(eventDataHolder.getDeleted(), DynamicConfigEventType.DELETE, null);
        }
        if (!eventDataHolder.getModified().isEmpty()) {
            // Modify event
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
            // Notify listener with a single key
            notifyEvent(entry.getKey(), entry.getValue(), dynamicConfigEventType, false, latestData);

            // Notify the group's listeners to update
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
                    // The notified listener is not notified again. This prevents multiple notifications for
                    // different keys of the same group
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
            // Required to be initialized, but not initialized, full data notification
            for (Map.Entry<String, String> entry : latestData.entrySet()) {
                versionListener.listener
                        .process(DynamicConfigEvent.initEvent(entry.getKey(), event.getGroup(), entry.getValue()));
            }
            versionListener.isInitializer = true;
            versionListener.initVersion = currentVersion;
        } else {
            if (versionListener.initVersion != 0 && versionListener.initVersion != currentVersion) {
                // For other add events that have been notified or do not need to be notified, check whether the
                // version of the initial notification is the current version before notification. If it is the
                // current version, it indicates that it has been fully notified and does not need to be notified again
                versionListener.listener.process(DynamicConfigEvent.createEvent(event.getKey(), event.getGroup(),
                        event.getContent()));
            }
        }
    }

    /**
     * Add key listener
     *
     * @param key key
     * @param dynamicConfigListener dynamic config listener
     * @param ifNotify if notify
     */
    public final void addKeyListener(String key, DynamicConfigListener dynamicConfigListener, boolean ifNotify) {
        VersionListenerWrapper versionListenerWrapper = keyListenerMap.get(key);
        if (versionListenerWrapper == null) {
            versionListenerWrapper = new VersionListenerWrapper();
        }
        versionListenerWrapper.addListener(dynamicConfigListener, ifNotify);
        keyListenerMap.put(key, versionListenerWrapper);
    }

    /**
     * Remove key listener
     *
     * @param key key
     * @param listener dynamic config listener
     * @return remove result
     */
    public boolean removeKeyListener(String key, DynamicConfigListener listener) {
        final VersionListenerWrapper versionListenerWrapper = keyListenerMap.get(key);
        if (versionListenerWrapper == null) {
            return false;
        }
        return versionListenerWrapper.removeListener(listener);
    }

    /**
     * Whether all listeners of the current group are cleared
     *
     * @return true if empty
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
     * Version listener wrapper
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
            // VersionListener is removed based on listener
            return listeners.remove(new VersionListener(-1L, listener, false));
        }
    }

    /**
     * VersionListener, added version judgment
     *
     * @since 2022-01-01
     */
    static class VersionListener {
        DynamicConfigListener listener;

        /**
         * The notified version, based on the KIE data version, also called revision
         */
        long version;

        /**
         * Whether to be notified by an INIT event. The event is notified only once
         */
        boolean isInitializer = false;

        /**
         * Whether first notification is required
         */
        boolean isNeedInit;

        /**
         * initial version
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
