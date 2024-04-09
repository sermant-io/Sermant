/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.monitor.util;

import com.huawei.monitor.config.MonitorServiceConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;

/**
 * monitoring tools
 *
 * @author zhp
 * @version 1.0.0
 * @since 2022-11-02
 */
public class MonitorUtil {
    private static final MonitorServiceConfig CONFIG = PluginConfigManager.getPluginConfig(MonitorServiceConfig.class);

    private MonitorUtil() {
    }

    /**
     * get monitor switch
     *
     * @return monitor switch
     */
    public static boolean startMonitor() {
        return CONFIG.isEnableStartService() && StringUtils.isNoneBlank(CONFIG.getReportType());
    }
}
