/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.connection.pool.collect.config;

import com.huawei.javamesh.core.config.common.ConfigTypeKey;
import com.huawei.javamesh.core.plugin.config.PluginConfig;

/**
 * Druid Monitor 配置
 */
@ConfigTypeKey("druid.monitor")
public class DruidMonitorConfig implements PluginConfig {
    private static final int DEFAULT_COLLECT_INTERVAL_SEC = 1;
    private static final int DEFAULT_CONSUME_INTERVAL_SEC = 10;
    private static final String DEFAULT_TIME_UNIT = "SECONDS";

    private long collectInterval = DEFAULT_COLLECT_INTERVAL_SEC;
    private long consumeInterval = DEFAULT_CONSUME_INTERVAL_SEC;
    private String timeunit = DEFAULT_TIME_UNIT;

    public long getCollectInterval() {
        return collectInterval;
    }

    public void setCollectInterval(long collectInterval) {
        this.collectInterval = collectInterval;
    }

    public long getConsumeInterval() {
        return consumeInterval;
    }

    public void setConsumeInterval(long consumeInterval) {
        this.consumeInterval = consumeInterval;
    }

    public String getTimeunit() {
        return timeunit;
    }

    public void setTimeunit(String timeunit) {
        this.timeunit = timeunit;
    }
}
