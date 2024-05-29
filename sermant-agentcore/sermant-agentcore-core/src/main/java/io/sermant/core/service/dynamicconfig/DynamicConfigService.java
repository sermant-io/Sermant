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

package io.sermant.core.service.dynamicconfig;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.service.BaseService;
import io.sermant.core.service.dynamicconfig.api.GroupService;
import io.sermant.core.service.dynamicconfig.api.KeyGroupService;
import io.sermant.core.service.dynamicconfig.api.KeyService;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import io.sermant.core.service.dynamicconfig.config.DynamicConfig;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Abstract class for dynamically configure service, performs null check on input parameters, all empty groups are
 * modified to the default group. key and listener are not allowed to be empty
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-14
 */
public abstract class DynamicConfigService implements BaseService, KeyService, KeyGroupService, GroupService {
    /**
     * Dynamic configuration information
     */
    protected static final DynamicConfig CONFIG = ConfigManager.getConfig(DynamicConfig.class);

    /**
     * A Logger object is used to log messages for a specific system or application component.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Fix the group, if the group is empty, use the default group instead
     *
     * @param group group
     * @return fixed group
     */
    protected String fixGroup(String group) {
        return group == null || group.isEmpty() ? CONFIG.getDefaultGroup() : group;
    }

    @Override
    public String getConfig(String key) {
        return getConfig(key, null);
    }

    @Override
    public String getConfig(String key, String group) {
        return doGetConfig(key, fixGroup(group)).orElse(null);
    }

    @Override
    public boolean publishConfig(String key, String content) {
        return publishConfig(key, null, content);
    }

    @Override
    public boolean publishConfig(String key, String group, String content) {
        if (!checkKey(key)) {
            return false;
        }
        if (content == null || content.isEmpty()) {
            LOGGER.warning("Publish empty config is meaningless, so skip. ");
            return false;
        }
        return doPublishConfig(key, fixGroup(group), content);
    }

    @Override
    public boolean removeConfig(String key) {
        return removeConfig(key, null);
    }

    @Override
    public boolean removeConfig(String key, String group) {
        if (!checkKey(key)) {
            return false;
        }
        return doRemoveConfig(key, fixGroup(group));
    }

    @Override
    public List<String> listKeys() {
        return listKeysFromGroup(null);
    }

    @Override
    public boolean addConfigListener(String key, DynamicConfigListener listener) {
        return addConfigListener(key, null, listener);
    }

    /**
     * Add a listener (default group) for a key that determines whether an initialization event is triggered based on
     * the input parameter
     *
     * @param key key
     * @param listener listener
     * @param ifNotify if notify
     * @return add result
     */
    public boolean addConfigListener(String key, DynamicConfigListener listener, boolean ifNotify) {
        return addConfigListener(key, null, listener, ifNotify);
    }

    /**
     * Add a listener for a key under the group and determines whether an initialization event is triggered based on the
     * input parameter
     *
     * @param key key
     * @param group group
     * @param listener listener
     * @param ifNotify if notify
     * @return add result
     */
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener, boolean ifNotify) {
        final boolean addResult = addConfigListener(key, group, listener);
        if (addResult && ifNotify) {
            listener.process(DynamicConfigEvent.initEvent(key, fixGroup(group), getConfig(key, group)));
        }
        return addResult;
    }

    @Override
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener) {
        if (!checkKey(key)) {
            return false;
        }
        if (listener == null) {
            LOGGER.warning("Empty listener is not allowed. ");
            return false;
        }
        return doAddConfigListener(key, fixGroup(group), listener);
    }

    @Override
    public boolean removeConfigListener(String key) {
        return removeConfigListener(key, null);
    }

    @Override
    public boolean removeConfigListener(String key, String group) {
        if (checkKey(key)) {
            return false;
        }
        return doRemoveConfigListener(key, fixGroup(group));
    }

    @Override
    public List<String> listKeysFromGroup(String group) {
        return doListKeysFromGroup(fixGroup(group));
    }

    @Override
    public boolean addGroupListener(String group, DynamicConfigListener listener) {
        if (listener == null) {
            LOGGER.warning("Empty listener is not allowed. ");
            return false;
        }
        return doAddGroupListener(fixGroup(group), listener);
    }

    /**
     * Add listeners for all keys under the group, and decide whether to fire an initialization event based on the input
     * parameter
     *
     * @param group group
     * @param listener listener
     * @param ifNotify if notify
     * @return add result
     */
    public boolean addGroupListener(String group, DynamicConfigListener listener, boolean ifNotify) {
        final boolean addResult = addGroupListener(group, listener);
        if (ifNotify && addResult) {
            for (String key : listKeysFromGroup(group)) {
                listener.process(DynamicConfigEvent.initEvent(key, fixGroup(group), getConfig(key, group)));
            }
        }
        return addResult;
    }

    @Override
    public boolean removeGroupListener(String group) {
        return doRemoveGroupListener(fixGroup(group));
    }

    /**
     * Get the configuration value for a key under the group
     *
     * @param key key
     * @param group group
     * @return Configuration value
     */
    public abstract Optional<String> doGetConfig(String key, String group);

    /**
     * Set the configuration value of a key under the group
     *
     * @param key key
     * @param group group
     * @param content config content
     * @return publish result
     */
    public abstract boolean doPublishConfig(String key, String group, String content);

    /**
     * Remove the configuration value of a key under the group
     *
     * @param key key
     * @param group group
     * @return remove result
     */
    public abstract boolean doRemoveConfig(String key, String group);

    /**
     * Adds a listener for a key under the group
     *
     * @param key key
     * @param group group
     * @param listener listener
     * @return add result
     */
    public abstract boolean doAddConfigListener(String key, String group, DynamicConfigListener listener);

    /**
     * Remove listeners for all keys under the group
     *
     * @param group group
     * @return remove result
     */
    public abstract boolean doRemoveGroupListener(String group);

    /**
     * Removes the listener for a key under the group
     *
     * @param key key
     * @param group group
     * @return remove result
     */
    public abstract boolean doRemoveConfigListener(String key, String group);

    /**
     * Gets all keys in the group
     *
     * @param group group
     * @return key list
     */
    public abstract List<String> doListKeysFromGroup(String group);

    /**
     * Add listeners for all keys under the group
     *
     * @param group group
     * @param listener listener
     * @return add result
     */
    public abstract boolean doAddGroupListener(String group, DynamicConfigListener listener);

    private boolean checkKey(String key) {
        if (key == null || key.isEmpty()) {
            LOGGER.warning("Empty key is not allowed. ");
            return false;
        }
        return true;
    }
}
