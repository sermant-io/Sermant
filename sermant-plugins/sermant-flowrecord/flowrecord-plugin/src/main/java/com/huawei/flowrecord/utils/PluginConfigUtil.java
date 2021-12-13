/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.flowrecord.utils;

import com.huawei.sermant.core.plugin.config.PluginConfigManager;
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
            flowRecordConfig = PluginConfigManager.getPluginConfig(FlowRecordConfig.class);
        } catch (Exception e) {
            LOGGER.info("[flowrecord]: cannot get properties");
        }
    }

    public static FlowRecordConfig getFlowRecordConfig() {
        return flowRecordConfig;
    }
}
