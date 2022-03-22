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

package com.huawei.flowcontrol.core.util;

import com.huawei.flowcontrol.adapte.cse.datasource.CseDataSourceManager;
import com.huawei.flowcontrol.common.config.FlowControlConfig;
import com.huawei.flowcontrol.core.datasource.DataSourceManager;
import com.huawei.flowcontrol.core.datasource.DefaultDataSourceManager;
import com.huawei.flowcontrol.core.datasource.zookeeper.ZookeeperDatasourceManager;
import com.huawei.sermant.core.plugin.config.PluginConfigManager;

/**
 * 初始化加载规则数据工具类
 *
 * @author hudeyu
 * @since 2021-01-22
 */
public class DataSourceInitUtils {
    private static DataSourceManager dataSourceManager;

    private DataSourceInitUtils() {
    }

    /**
     * 初始化加载规则数据从配置中心
     */
    public static synchronized void initRules() {
        final FlowControlConfig pluginConfig = PluginConfigManager.getPluginConfig(FlowControlConfig.class);
        if (pluginConfig.isUseCseRule()) {
            // 适配cse场景
            dataSourceManager = new CseDataSourceManager();
        } else {
            // 原来场景
            dataSourceManager = new DefaultDataSourceManager();
        }
        if (pluginConfig.isAdaptPass() && "zookeeper".equals(pluginConfig.getConfigCenterType())) {
            dataSourceManager = new ZookeeperDatasourceManager();
        }
        dataSourceManager.start();
    }

    /**
     * 停止方法
     */
    public static synchronized void stop() {
        if (dataSourceManager != null) {
            dataSourceManager.stop();
        }
    }
}
