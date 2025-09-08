/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.dynamicconfig.apollo;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChange;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.service.dynamicconfig.DynamicConfigService;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.implement.service.dynamicconfig.apollo.config.ApolloProperty;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dynamic configuration service, Apollo implementation
 *
 * @author Chen Zhenyang
 * @since 2025-08-07
 */
public class ApolloDynamicConfigService extends DynamicConfigService {
    /**
     * logger
     */
    private static final Logger LOGGER = Logger.getLogger(ApolloDynamicConfigService.class.getName());

    private ApolloBufferedClient apolloClient;

    private final ServiceMeta serviceMeta;

    private final Map<String, List<ConfigChangeListener>> groupListeners = new ConcurrentHashMap<>();

    private final Map<String, Map<String, List<ConfigChangeListener>>> keyListeners = new ConcurrentHashMap<>();

    /**
     * constructor
     */
    public ApolloDynamicConfigService() {
        serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
    }

    @Override
    public void start() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("app_id", serviceMeta.getApplication());
        params.put("cluster", serviceMeta.getParameters().get("cluster"));
        params.put("env", serviceMeta.getEnvironment());
        params.put("url", CONFIG.getServerAddress());
        params.put("access_key", CONFIG.getPrivateKey());
        params.put("token", serviceMeta.getParameters().get("token"));
        params.put("read_timeout", String.valueOf(CONFIG.getTimeoutValue()));
        params.put("connect_timeout", String.valueOf(CONFIG.getConnectTimeout()));
        params.put("admin_url", serviceMeta.getParameters().get("adminUrl"));
        params.put("user", CONFIG.getUserName());
        params.put("double_check", serviceMeta.getParameters().get("isDoubleCheck"));
        ApolloProperty.setProperties(params);
        apolloClient = new ApolloBufferedClient();
    }

    @Override
    public void stop() {
        apolloClient.close();
    }

    @Override
    public Optional<String> doGetConfig(String key, String group) {
        if (!ApolloUtils.isValidNamespace(group)) {
            LOGGER.log(Level.SEVERE, "apollo get config failed, group name is invalid. group: {0}", group);
            return Optional.empty();
        }
        String validGroup = ApolloUtils.rebuildGroup(group);
        Optional<String> config = Optional.ofNullable(apolloClient.getConfig(key, validGroup));

        LOGGER.log(Level.INFO, "apollo config get success, key: {0}, group: {1}",
                new String[]{key, validGroup});
        return config;
    }

    @Override
    public boolean doPublishConfig(String key, String group, String content) {
        if (!ApolloUtils.isValidNamespace(group)) {
            LOGGER.log(Level.SEVERE, "apollo publish config failed, group name is invalid. group: {0}", group);
            return false;
        }
        String validGroup = ApolloUtils.rebuildGroup(group);

        boolean result = apolloClient.publishConfig(key, validGroup, content);
        if (result) {
            LOGGER.log(Level.INFO, "apollo config publish success, key: {0}, group: {1}, content: {2}",
                    new String[]{key, validGroup, content});
        } else {
            LOGGER.log(Level.SEVERE, "apollo config publish failed, key: {0}, group: {1}, content: {2}",
                    new String[]{key, validGroup, content});
        }
        return result;
    }

    @Override
    public boolean doRemoveConfig(String key, String group) {
        if (!ApolloUtils.isValidNamespace(group)) {
            LOGGER.log(Level.SEVERE, "apollo remove config failed, group name is invalid. group: {0}", group);
            return false;
        }
        String validGroup = ApolloUtils.rebuildGroup(group);

        boolean result = apolloClient.removeConfig(key, validGroup);
        if (result) {
            LOGGER.log(Level.INFO, "apollo config remove success, key: {0}, group: {1}",
                    new String[]{key, validGroup});
        } else {
            LOGGER.log(Level.SEVERE, "apollo config remove failed, key: {0}, group: {1}",
                    new String[]{key, validGroup});
        }
        return result;
    }

    @Override
    public boolean doAddConfigListener(String key, String group, DynamicConfigListener listener) {
        if (!ApolloUtils.isValidNamespace(group)) {
            LOGGER.log(Level.SEVERE, "apollo config listener add failed, group name is invalid. group: {0}", group);
            return false;
        }
        String validGroup = ApolloUtils.rebuildGroup(group);
        ConfigChangeListener apolloListener = event -> {
            for (String k : event.changedKeys()) {
                if (k.equals(key)) {
                    listener.process(transEvent(k, group, event.getChange(k)));
                }
            }
        };
        boolean result = apolloClient.addConfigListener(key, validGroup, apolloListener);
        if (result) {
            keyListeners.putIfAbsent(validGroup, new ConcurrentHashMap<>());
            Map<String, List<ConfigChangeListener>> keyListener = keyListeners.get(validGroup);
            List<ConfigChangeListener> listenerList = keyListener.getOrDefault(key, new CopyOnWriteArrayList<>());
            listenerList.add(apolloListener);
            keyListener.put(key, listenerList);
            LOGGER.log(Level.INFO, "apollo config listener add success, key: {0}, group: {1}",
                    new String[]{key, validGroup});
        } else {
            LOGGER.log(Level.SEVERE, "apollo config listener add failed, key: {0}, group: {1}",
                    new String[]{key, validGroup});
        }
        return result;
    }

    /**
     * transform apollo event to sermant inner event
     *
     * @param key key of apollo
     * @param group sermant group
     * @param change change type
     * @return sermant dynamic config event
     */
    public DynamicConfigEvent transEvent(String key, String group, ConfigChange change) {
        switch (change.getChangeType()) {
            case ADDED: {
                return DynamicConfigEvent.createEvent(key, group, change.getNewValue());
            }
            case DELETED: {
                return DynamicConfigEvent.deleteEvent(key, group, null);
            }
            case MODIFIED:
            default: {
                return DynamicConfigEvent.modifyEvent(key, group, change.getNewValue());
            }
        }
    }

    @Override
    public boolean doRemoveConfigListener(String key, String group) {
        if (!ApolloUtils.isValidNamespace(group)) {
            LOGGER.log(Level.SEVERE, "apollo config listener remove failed, group name is invalid. group: {0}", group);
            return false;
        }
        String validGroup = ApolloUtils.rebuildGroup(group);
        Map<String, List<ConfigChangeListener>> keyListener = keyListeners.getOrDefault(validGroup, null);
        if (keyListener == null) {
            return true;
        }
        List<ConfigChangeListener> listenerList = keyListener.getOrDefault(key, null);
        if (listenerList == null) {
            return true;
        }
        for (ConfigChangeListener listener : listenerList) {
            if (!apolloClient.removeConfigListener(validGroup, listener)) {
                LOGGER.log(Level.SEVERE, "apollo config listener remove failed. key: {0}, group: {1}",
                        new String[]{key, validGroup});
            }
        }
        keyListener.remove(key);
        if (keyListener.isEmpty()) {
            keyListeners.remove(validGroup);
        }
        LOGGER.log(Level.INFO, "apollo config listener remove success, key: {0}, group: {1}",
                new String[]{key, validGroup});
        return true;
    }

    @Override
    public boolean doAddGroupListener(String group, DynamicConfigListener listener) {
        if (!ApolloUtils.isValidNamespace(group)) {
            LOGGER.log(Level.SEVERE, "apollo group listener add failed, group name is invalid. group: {0}", group);
            return false;
        }
        String validGroup = ApolloUtils.rebuildGroup(group);
        ConfigChangeListener apolloListener = event -> {
            for (String k : event.changedKeys()) {
                listener.process(transEvent(k, group, event.getChange(k)));
            }
        };
        boolean result = apolloClient.addGroupListener(group, apolloListener);
        if (result) {
            groupListeners.putIfAbsent(validGroup, new CopyOnWriteArrayList<>());
            List<ConfigChangeListener> listeners = groupListeners.get(validGroup);

            listeners.add(apolloListener);
            LOGGER.log(Level.INFO, "apollo group listener add success, group: {0}",
                    new String[]{validGroup});
        } else {
            LOGGER.log(Level.SEVERE, "apollo group listener add failed, group: {0}",
                    new String[]{validGroup});
        }
        return result;
    }

    @Override
    public boolean doRemoveGroupListener(String group) {
        if (!ApolloUtils.isValidNamespace(group)) {
            LOGGER.log(Level.SEVERE, "apollo group listener remove failed, group name is invalid. group: {0}", group);
            return false;
        }
        String validGroup = ApolloUtils.rebuildGroup(group);
        List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
        if (groupListeners.containsKey(validGroup)) {
            listeners.addAll(groupListeners.get(validGroup));
            groupListeners.remove(validGroup);
        }
        if (keyListeners.containsKey(validGroup)) {
            Map<String, List<ConfigChangeListener>> stringSetMap = keyListeners.get(validGroup);
            for (String key : stringSetMap.keySet()) {
                listeners.addAll(stringSetMap.get(key));
                stringSetMap.remove(key);
            }
            keyListeners.remove(validGroup);
        }

        for (ConfigChangeListener ccl : listeners) {
            if (!apolloClient.removeConfigListener(validGroup, ccl)) {
                LOGGER.log(Level.SEVERE, "apollo group listener remove failed, group: {0}", new String[]{validGroup});
            }
        }
        LOGGER.log(Level.INFO, "apollo group listener remove success, group: {0}", new String[]{validGroup});
        return true;
    }

    @Override
    public List<String> doListKeysFromGroup(String group) {
        if (!ApolloUtils.isValidNamespace(group)) {
            LOGGER.log(Level.SEVERE, "Apollo list keys from group failed, group name is invalid. group: {0}",
                    group);
            return Collections.emptyList();
        }
        String validGroup = ApolloUtils.rebuildGroup(group);
        List<String> resultList = apolloClient.getListKeysFromGroup(validGroup);
        LOGGER.log(Level.INFO, "apollo config list get success, group: {0}", new String[]{validGroup});
        return CollectionUtils.isEmpty(resultList) ? Collections.emptyList() : resultList;
    }
}
