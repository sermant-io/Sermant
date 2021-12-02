/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.util;

import com.huawei.javamesh.core.plugin.config.PluginConfigManager;
import com.huawei.flowcontrol.adapte.cse.datasource.CseDataSourceManager;
import com.huawei.flowcontrol.core.config.FlowControlConfig;
import com.huawei.flowcontrol.core.datasource.DataSourceManager;
import com.huawei.flowcontrol.core.datasource.DefaultDataSourceManager;
import com.huawei.flowcontrol.core.datasource.zookeeper.ZookeeperDatasourceManager;

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
