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

package com.huawei.sermant.core.service.dynamicconfig.kie.client.kie;

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.service.dynamicconfig.kie.listener.KvDataHolder;
import com.huawei.sermant.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigChangeType;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigChangeEvent;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigListener;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * listener封装，关联任务执行器
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KieListenerWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private DynamicConfigListener dynamicConfigListener;

    private SubscriberManager.Task task;

    private String group;

    private final KvDataHolder kvDataHolder;

    public void notifyListener(KvDataHolder.EventDataHolder eventDataHolder) {
        if (!eventDataHolder.getAdded().isEmpty()) {
            // 新增事件
            notify(eventDataHolder.getAdded(), DynamicConfigChangeType.ADDED);
        }
        if (!eventDataHolder.getDeleted().isEmpty()) {
            // 删除事件
            notify(eventDataHolder.getDeleted(), DynamicConfigChangeType.DELETED);
        }
        if (!eventDataHolder.getModified().isEmpty()) {
            // 修改事件
            notify(eventDataHolder.getModified(), DynamicConfigChangeType.MODIFIED);
        }
    }

    private void notify(Map<String, String> configData, DynamicConfigChangeType dynamicConfigChangeType) {
        for (Map.Entry<String, String> entry : configData.entrySet()) {
            try {
                dynamicConfigListener.process(new DynamicConfigChangeEvent(entry.getKey(), this.group, entry.getValue(),
                        dynamicConfigChangeType));
            } catch (Throwable ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Process config data failed, key: [%s], group: [%s]",
                        entry.getKey(), this.group));
            }
        }
    }

    public KieListenerWrapper(DynamicConfigListener dynamicConfigListener, SubscriberManager.Task task,
                              KvDataHolder kvDataHolder) {
        this.dynamicConfigListener = dynamicConfigListener;
        this.task = task;
        this.kvDataHolder = kvDataHolder;
    }

    public KieListenerWrapper(String group, DynamicConfigListener dynamicConfigListener, KvDataHolder kvDataHolder) {
        this.group = group;
        this.dynamicConfigListener = dynamicConfigListener;
        this.kvDataHolder = kvDataHolder;
    }

    public DynamicConfigListener getConfigurationListener() {
        return dynamicConfigListener;
    }

    public void setConfigurationListener(DynamicConfigListener dynamicConfigListener) {
        this.dynamicConfigListener = dynamicConfigListener;
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
