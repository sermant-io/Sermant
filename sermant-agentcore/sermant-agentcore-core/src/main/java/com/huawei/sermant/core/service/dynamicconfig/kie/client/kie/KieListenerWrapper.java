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
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huawei.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
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
            notify(eventDataHolder.getAdded(), DynamicConfigEventType.CREATE);
        }
        if (!eventDataHolder.getDeleted().isEmpty()) {
            // 删除事件
            notify(eventDataHolder.getDeleted(), DynamicConfigEventType.DELETE);
        }
        if (!eventDataHolder.getModified().isEmpty()) {
            // 修改事件
            notify(eventDataHolder.getModified(), DynamicConfigEventType.MODIFY);
        }
    }

    private void notify(Map<String, String> configData, DynamicConfigEventType dynamicConfigEventType) {
        for (Map.Entry<String, String> entry : configData.entrySet()) {
            try {
                notifyEvent(entry.getKey(), entry.getValue(), dynamicConfigEventType);
            } catch (Throwable ex) {
                LOGGER.warning(String.format(Locale.ENGLISH, "Process config data failed, key: [%s], group: [%s]",
                        entry.getKey(), this.group));
            }
        }
    }

    private void notifyEvent(String key, String value, DynamicConfigEventType eventType) {
        switch (eventType) {
            case CREATE:
                dynamicConfigListener.process(DynamicConfigEvent.createEvent(key, this.group, value));
                break;
            case MODIFY:
                dynamicConfigListener.process(DynamicConfigEvent.modifyEvent(key, this.group, value));
                break;
            case DELETE:
                dynamicConfigListener.process(DynamicConfigEvent.deleteEvent(key, this.group, value));
                break;
            default:
                LOGGER.warning(String.format(Locale.ENGLISH, "Event type [%s] is invalid. ", eventType));
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
