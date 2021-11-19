/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.config.kie;

import com.huawei.config.listener.ConfigurationListener;
import com.huawei.config.listener.KvDataHolder;
import com.huawei.config.listener.SubscriberManager;

/**
 * listener封装，关联任务执行器
 *
 * @author zhouss
 * @since 2021-11-18
 */
public class KieListenerWrapper {
    private ConfigurationListener configurationListener;

    private SubscriberManager.Task task;

    private KvDataHolder kvDataHolder;

    public KieListenerWrapper(ConfigurationListener configurationListener, SubscriberManager.Task task,
                              KvDataHolder kvDataHolder) {
        this.configurationListener = configurationListener;
        this.task = task;
        this.kvDataHolder = kvDataHolder;
    }

    public KieListenerWrapper(ConfigurationListener configurationListener, KvDataHolder kvDataHolder) {
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
