/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.connection.pool.collect.config;

import com.huawei.apm.core.config.common.ConfigTypeKey;
import com.huawei.apm.core.plugin.config.PluginConfig;
import lombok.Data;

/**
 * Druid Monitor 配置
 */
@Data
@ConfigTypeKey("druid.monitor")
public class DruidMonitorConfig implements PluginConfig {
    private static final int DEFAULT_COLLECT_INTERVAL_SEC = 1;
    private static final int DEFAULT_CONSUME_INTERVAL_SEC = 10;
    private static final String DEFAULT_TIME_UNIT = "SECONDS";

    private long collectInterval = DEFAULT_COLLECT_INTERVAL_SEC;
    private long consumeInterval = DEFAULT_CONSUME_INTERVAL_SEC;
    private String timeunit = DEFAULT_TIME_UNIT;
}
