/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowrecord.utils;

import com.huawei.apm.core.config.ConfigLoader;
import com.huawei.flowrecord.config.FlowRecordConfig;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;

/**
 * 配置信息工具类
 */
public class PluginConfigUtil {
    private static final ILog LOGGER = LogManager.getLogger(PluginConfigUtil.class);
    private static FlowRecordConfig flowRecordConfig;

    static {
        try {
            flowRecordConfig = ConfigLoader.getConfig(FlowRecordConfig.class);
        } catch (Exception e) {
            LOGGER.info("[flowrecord]: cannot get properties");
        }
    }

    public static FlowRecordConfig getFlowRecordConfig() {
        return flowRecordConfig;
    }
}
