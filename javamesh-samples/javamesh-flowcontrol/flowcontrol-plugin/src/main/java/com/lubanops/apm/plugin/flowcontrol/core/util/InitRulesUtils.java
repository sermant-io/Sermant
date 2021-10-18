/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.util;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.lubanops.apm.plugin.flowcontrol.core.config.ConfigConst;
import com.lubanops.apm.plugin.flowcontrol.core.datasource.DataSourceManager;
import com.lubanops.apm.plugin.flowcontrol.core.datasource.kie.KieDataSourceManager;
import com.lubanops.apm.plugin.flowcontrol.core.datasource.zookeeper.ZookeeperDatasourceManager;

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
        RecordLog.info("dataSourceType: " + PluginConfigUtil.getValueByKey(ConfigConst.SENTINEL_CONFIG_TYPE));
        DataSourceManager dataSourceManager = new ZookeeperDatasourceManager();
        if ("servicecomb-kie".equals(PluginConfigUtil.getValueByKey(ConfigConst.SENTINEL_CONFIG_TYPE))) {
            dataSourceManager = new KieDataSourceManager();
        }
        dataSourceManager.initRules();
    }
}
