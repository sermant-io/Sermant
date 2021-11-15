/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.labels.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 心跳健康配置
 *
 * @author zhouss
 * @since 2021-10-30
 */
@Component
public class AgentHeartbeatConfig {
    /**
     * 默认心跳存活时间 单位：毫秒
     */
    public static final int DEFAULT_MACHINE_HEALTHY_TIMEOUT_MS = 60_000;

    private static int HELTHY_TIMEOUT;

    @Value("${agent.heartbeat.unhealthyMachineMillis:60000}")
    public void setHelthyTimeout(int timeout) {
        HELTHY_TIMEOUT = timeout;
    }

    /**
     * 获取判定存活的心跳时间
     *
     * @return 存活时间
     */
    public static int getUnhealthyMachineMillis() {
        if (HELTHY_TIMEOUT != 0) {
            return HELTHY_TIMEOUT;
        } else {
            return DEFAULT_MACHINE_HEALTHY_TIMEOUT_MS;
        }
    }
}
