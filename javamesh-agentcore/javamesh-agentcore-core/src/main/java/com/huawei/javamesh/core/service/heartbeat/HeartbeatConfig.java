/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.core.service.heartbeat;

import com.huawei.javamesh.core.config.common.BaseConfig;
import com.huawei.javamesh.core.config.common.ConfigTypeKey;

/**
 * 心跳配置
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/29
 */
@ConfigTypeKey("heartbeat")
public class HeartbeatConfig implements BaseConfig {
    /**
     * 心跳发送间隔
     */
    private long interval = 3000;

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }
}
