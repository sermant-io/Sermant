/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.util;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.huawei.apm.core.service.dynamicconfig.Config;
import com.huawei.apm.core.service.dynamicconfig.service.DynamicConfigType;
import com.huawei.flowcontrol.adapte.cse.datasource.CseDataSourceManager;
import com.huawei.flowcontrol.core.config.CommonConst;
import com.huawei.flowcontrol.core.datasource.DataSourceManager;
import com.huawei.flowcontrol.core.datasource.kie.KieDataSourceManager;
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
    public static synchronized void initRules(String configCenterType) {
        RecordLog.info("dataSourceType: " + configCenterType);
        if (CommonConst.CONFIG_TYPE_KIE.equals(configCenterType)
                && Config.getDynamic_config_type() == DynamicConfigType.KIE) {
            dataSourceManager = new KieDataSourceManager();
        } else if (CommonConst.CONFIG_TYPE_CSE_KIE.equals(configCenterType)
                && Config.getDynamic_config_type() == DynamicConfigType.KIE) {
            dataSourceManager = new CseDataSourceManager();
        } else {
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
