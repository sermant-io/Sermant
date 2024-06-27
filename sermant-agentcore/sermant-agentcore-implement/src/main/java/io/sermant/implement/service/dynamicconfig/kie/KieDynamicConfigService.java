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

package io.sermant.implement.service.dynamicconfig.kie;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.dynamicconfig.DynamicConfigService;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import io.sermant.implement.service.dynamicconfig.kie.client.kie.KieConfigEntity;
import io.sermant.implement.service.dynamicconfig.kie.client.kie.KieResponse;
import io.sermant.implement.service.dynamicconfig.kie.constants.KieConstants;
import io.sermant.implement.service.dynamicconfig.kie.listener.SubscriberManager;
import io.sermant.implement.utils.LabelGroupUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Kie Configuration center implementation
 *
 * @author zhouss
 * @since 2021-11-22
 */
public class KieDynamicConfigService extends DynamicConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static SubscriberManager subscriberManager;

    /**
     * KieDynamicConfigService
     */
    public KieDynamicConfigService() {
        subscriberManager = new SubscriberManager(CONFIG.getServerAddress(), CONFIG.getTimeoutValue());
    }

    /**
     * KieDynamicConfigService
     *
     * @param serverAddress serverAddress
     * @param project namespace
     */
    public KieDynamicConfigService(String serverAddress, String project) {
        subscriberManager = new SubscriberManager(serverAddress, project, CONFIG.getTimeoutValue());
    }

    @Override
    public boolean doRemoveGroupListener(String group) {
        return updateGroupListener(group, null, false, false);
    }

    @Override
    public boolean doAddGroupListener(String group, DynamicConfigListener listener) {
        return addGroupListener(group, listener, false);
    }

    @Override
    public boolean addGroupListener(String group, DynamicConfigListener listener, boolean ifNotify) {
        return updateGroupListener(group, listener, true, ifNotify);
    }

    @Override
    public boolean doAddConfigListener(String key, String group, DynamicConfigListener listener) {
        return addConfigListener(key, group, listener, false);
    }

    @Override
    public boolean addConfigListener(String key, String group, DynamicConfigListener listener, boolean ifNotify) {
        String newGroup = group;
        if (!LabelGroupUtils.isLabelGroup(group)) {
            // Add label group judgment to adapt irregular groups
            newGroup = LabelGroupUtils.createLabelGroup(Collections.singletonMap(KieConstants.DEFAULT_GROUP,
                    fixSeparator(group, true)));
        }
        return subscriberManager.addConfigListener(key, newGroup, listener, ifNotify);
    }

    @Override
    public boolean doRemoveConfigListener(String key, String group) {
        // Removing a single listener is not supported
        return false;
    }

    @Override
    public Optional<String> doGetConfig(String key, String group) {
        final KieResponse kieResponse =
                subscriberManager.queryConfigurations(null, LabelGroupUtils.getLabelCondition(group));
        if (!isValidResponse(kieResponse)) {
            return Optional.empty();
        }
        final List<KieConfigEntity> data = kieResponse.getData();
        for (KieConfigEntity entity : data) {
            if (key.equals(entity.getKey())) {
                return Optional.ofNullable(entity.getValue());
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean doPublishConfig(String key, String group, String content) {
        return subscriberManager.publishConfig(key, group, content);
    }

    @Override
    public boolean doRemoveConfig(String key, String group) {
        return subscriberManager.removeConfig(key, group);
    }

    @Override
    public List<String> doListKeysFromGroup(String group) {
        final KieResponse kieResponse =
                subscriberManager.queryConfigurations(null, LabelGroupUtils.getLabelCondition(group));
        if (isValidResponse(kieResponse)) {
            final List<KieConfigEntity> data = kieResponse.getData();
            final List<String> keys = new ArrayList<>(data.size());
            for (KieConfigEntity entity : data) {
                keys.add(entity.getKey());
            }
            return keys;
        }
        return Collections.emptyList();
    }

    private boolean isValidResponse(KieResponse kieResponse) {
        return kieResponse != null && kieResponse.getData() != null;
    }

    /**
     * Update listener (delete or add). If a listener is added for the first time, the listener is notified of the data
     *
     * @param group group. Method of generating groups specifically for KIE processing ->
     * LabelGroupUtils#createLabelGroup(Map)
     *
     * @param listener The listener of the corresponding group
     * @param forSubscribe whether for subscription
     * @param ifNotify Whether to notify when the listener is added for the first time
     * @return update result
     */
    private synchronized boolean updateGroupListener(String group, DynamicConfigListener listener, boolean forSubscribe,
            boolean ifNotify) {
        if (listener == null) {
            LOGGER.warning("Empty listener is not allowed. ");
            return false;
        }
        try {
            if (forSubscribe) {
                return subscriberManager.addGroupListener(fixGroup(group), listener, ifNotify);
            } else {
                return subscriberManager.removeGroupListener(fixGroup(group), listener);
            }
        } catch (Exception exception) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Subscribed kie request failed! raw group : %s", group));
            return false;
        }
    }

    /**
     * Remove the path separator
     *
     * @param str key or group
     * @param isGroup Group or not
     * @return fixed value
     * @throws IllegalArgumentException Exception thrown when key is empty
     */
    private String fixSeparator(String str, boolean isGroup) {
        String newStr = str;
        if (str == null) {
            if (isGroup) {
                // default group
                newStr = CONFIG.getDefaultGroup();
            } else {
                throw new IllegalArgumentException("Key must not be empty!");
            }
        }
        return newStr.startsWith("/") ? newStr.substring(1) : newStr;
    }
}
