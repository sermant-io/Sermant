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

package com.huawei.sermant.backend.service.dynamicconfig.kie.client.kie;

import com.huawei.sermant.backend.service.dynamicconfig.kie.listener.KvDataHolder;
import com.huawei.sermant.backend.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.sermant.backend.service.dynamicconfig.service.ConfigChangeType;
import com.huawei.sermant.backend.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.sermant.backend.service.dynamicconfig.service.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

/**
 * listener封装，关联任务执行器
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KieListenerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(KieListenerWrapper.class);

    private ConfigurationListener configurationListener;

    private SubscriberManager.Task task;

    private String group;

    private final KvDataHolder kvDataHolder;

    /**
     * listener封装，关联任务执行器
     *
     * @param group group
     * @param configurationListener listen
     * @param kvDataHolder  holder
     */
    public KieListenerWrapper(String group, ConfigurationListener configurationListener, KvDataHolder kvDataHolder) {
        this.group = group;
        this.configurationListener = configurationListener;
        this.kvDataHolder = kvDataHolder;
    }

    public void notifyListener(KvDataHolder.EventDataHolder eventDataHolder) {
        if (!eventDataHolder.getAdded().isEmpty()) {
            // 新增事件
            notify(eventDataHolder.getAdded(), ConfigChangeType.ADDED);
        }
        if (!eventDataHolder.getDeleted().isEmpty()) {
            // 删除事件
            notify(eventDataHolder.getDeleted(), ConfigChangeType.DELETED);
        }
        if (!eventDataHolder.getModified().isEmpty()) {
            // 修改事件
            notify(eventDataHolder.getModified(), ConfigChangeType.MODIFIED);
        }
    }

    private void notify(Map<String, String> configData, ConfigChangeType configChangeType) {
        for (Map.Entry<String, String> entry : configData.entrySet()) {
            try {
                configurationListener.process(new ConfigChangedEvent(entry.getKey(), this.group, entry.getValue(),
                        configChangeType));
            } catch (Throwable ex) {
                LOGGER.warn(String.format(Locale.ENGLISH, "Process config data failed, key: [%s], group: [%s]",
                        entry.getKey(), this.group));
            }
        }
    }

    public ConfigurationListener getConfigurationListener() {
        return configurationListener;
    }

    public void setConfigurationListener(ConfigurationListener configurationListener) {
        this.configurationListener = configurationListener;
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
}
