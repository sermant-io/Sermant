/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.huawei.fowcontrol.res4j.util;

import com.huawei.flowcontrol.common.config.MetricConfig;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.monitor.config.MonitorConfig;

/**
 * 监控工具类
 *
 * @author zhp
 * @since 2022-09-16
 */
public class MonitorUtils {
    private static MonitorConfig monitorConfig;

    private static MetricConfig metricConfig;

    private MonitorUtils() {
    }

    /**
     * 获取监控开关情况
     *
     * @return 开关
     */
    public static boolean isStartMonitor() {
        if (monitorConfig == null) {
            monitorConfig = ConfigManager.getConfig(MonitorConfig.class);
        }
        if (metricConfig == null) {
            metricConfig = PluginConfigManager.getPluginConfig(MetricConfig.class);
        }
        return monitorConfig != null && monitorConfig.isStartMonitor() && metricConfig != null
                && metricConfig.isEnableStartMonitor();
    }
}
