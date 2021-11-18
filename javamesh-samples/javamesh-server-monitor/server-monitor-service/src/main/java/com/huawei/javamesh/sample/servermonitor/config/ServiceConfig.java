/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.config;

import com.huawei.apm.core.config.common.ConfigTypeKey;
import com.huawei.apm.core.plugin.config.PluginConfig;
import lombok.Data;

@Data
@ConfigTypeKey("service.config")
public class ServiceConfig implements PluginConfig {
    private String service; // @nonnull
    private String serviceInstance; // @nonnull
}
