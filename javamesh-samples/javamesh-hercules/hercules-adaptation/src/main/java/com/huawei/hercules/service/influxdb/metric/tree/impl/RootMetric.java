/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree.impl;

import com.huawei.hercules.service.influxdb.SqlParam;
import com.huawei.hercules.service.influxdb.metric.tree.BaseMetric;
import com.huawei.hercules.service.influxdb.metric.tree.MetricInstanceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能描述：根指标
 *
 * @author z30009938
 * @since 2021-11-19
 */
public class RootMetric extends BaseMetric {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RootMetric.class);

    /**
     * 定义根节点的类型和数据
     */
    public RootMetric() {
        super("data", "");
        LOGGER.debug("Start init root metric.");
    }

    /**
     * 指标树初始化方法
     *
     * @param sqlParam 初始化各指标的参数
     */
    public void initTree(SqlParam sqlParam) {
        initMetric(MetricInstanceLoader.getMetrics(), sqlParam);
    }
}
