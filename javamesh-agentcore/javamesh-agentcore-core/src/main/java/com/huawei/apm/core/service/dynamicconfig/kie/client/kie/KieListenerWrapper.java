/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.apm.core.service.dynamicconfig.kie.client.kie;

import com.huawei.apm.core.common.LoggerFactory;
import com.huawei.apm.core.service.dynamicconfig.kie.listener.KvDataHolder;
import com.huawei.apm.core.service.dynamicconfig.kie.listener.SubscriberManager;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangeType;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigChangedEvent;
import com.huawei.apm.core.service.dynamicconfig.service.ConfigurationListener;

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

    private ConfigurationListener configurationListener;

    private SubscriberManager.Task task;

    private String group;

    private final KvDataHolder kvDataHolder;

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
                LOGGER.warning(String.format(Locale.ENGLISH, "Process config data failed, key: [%s], group: [%s]",
                        entry.getKey(), this.group));
            }
        }
    }

    public KieListenerWrapper(ConfigurationListener configurationListener, SubscriberManager.Task task,
                              KvDataHolder kvDataHolder) {
        this.configurationListener = configurationListener;
        this.task = task;
        this.kvDataHolder = kvDataHolder;
    }

    public KieListenerWrapper(String group, ConfigurationListener configurationListener, KvDataHolder kvDataHolder) {
        this.group = group;
        this.configurationListener = configurationListener;
        this.kvDataHolder = kvDataHolder;
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
