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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * 监控工具测试类
 *
 * @author zhp
 * @since 2022-09-20
 */
public class MonitorUtilsTest {
    @Test
    public void testSwitch() {
        MonitorConfig monitorConfig = new MonitorConfig();
        MetricConfig metricConfig = new MetricConfig();
        monitorConfig.setStartMonitor(true);
        metricConfig.setEnableStartMonitor(true);
        try (MockedStatic<ConfigManager> configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
             MockedStatic<PluginConfigManager> pluginConfigManagerMockedStatic =
                     Mockito.mockStatic(PluginConfigManager.class)) {
            configManagerMockedStatic.when(() -> ConfigManager.getConfig(MonitorConfig.class))
                    .thenReturn(monitorConfig);
            pluginConfigManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(MetricConfig.class))
                    .thenReturn(metricConfig);
            Assert.assertTrue(MonitorUtils.isStartMonitor());
        }
    }
}
