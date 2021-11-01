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
    public static final int DEFAULT_MACHINE_HEALTHY_TIMEOUT_MS = 60_000;

    private static int HELTHY_TIMEOUT;

    @Value("${agent.heartbeat.unhealthyMachineMillis:60000}")
    public void setHelthyTimeout(int timeout){
        HELTHY_TIMEOUT = timeout;
    }

    public static int getUnhealthyMachineMillis() {
        //huawei update change log: 心跳健康时间读取配置文件，配置为0，取默认值
        if(HELTHY_TIMEOUT!=0){
            return HELTHY_TIMEOUT;
        }else{
            return DEFAULT_MACHINE_HEALTHY_TIMEOUT_MS;
        }
    }
}
