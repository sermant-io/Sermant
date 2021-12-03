/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.monitor.common.config;

import com.huawei.javamesh.core.config.common.ConfigTypeKey;
import com.huawei.javamesh.core.plugin.config.PluginConfig;

@ConfigTypeKey("service.config")
public class ServiceConfig implements PluginConfig {
    private String service; // @nonnull
    private String serviceInstance; // @nonnull

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(String serviceInstance) {
        this.serviceInstance = serviceInstance;
    }
}
