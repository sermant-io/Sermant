/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.util;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.huawei.flowcontrol.adapte.cse.datasource.CseDataSourceManager;
import com.huawei.flowcontrol.core.config.ConfigConst;
import com.huawei.flowcontrol.core.datasource.DataSourceManager;
import com.huawei.flowcontrol.core.datasource.kie.KieDataSourceManager;
import com.huawei.flowcontrol.core.datasource.zookeeper.ZookeeperDatasourceManager;

/**
 * 初始化加载规则数据工具类
 *
 * @author hudeyu
 * @since 2021-01-22
 */
public class InitRulesUtils {
    private InitRulesUtils() {
    }

    /**
     * 初始化加载规则数据从配置中心
     */
    public static void initRules() {
        /*RecordLog.info("dataSourceType: " + PluginConfigUtil.getValueByKey(ConfigConst.SENTINEL_CONFIG_TYPE));
        DataSourceManager dataSourceManager = new ZookeeperDatasourceManager();
        if ("servicecomb-kie".equals(PluginConfigUtil.getValueByKey(ConfigConst.SENTINEL_CONFIG_TYPE))) {
            dataSourceManager = new KieDataSourceManager();
        }
        dataSourceManager.initRules();*/
        DataSourceManager dataSourceManager = new CseDataSourceManager();
        dataSourceManager.initRules();
    }
}
