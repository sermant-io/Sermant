/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree.impl;

import com.huawei.hercules.controller.monitor.dto.MonitorHostDTO;
import com.huawei.hercules.service.influxdb.metric.tree.BaseMetricNode;
import com.huawei.hercules.service.influxdb.metric.tree.MetricInstanceLoader;
import com.huawei.hercules.service.influxdb.metric.tree.MetricType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能描述：根指标
 *
 * @author z30009938
 * @since 2021-11-19
 */
public class RootMetricNode extends BaseMetricNode {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RootMetricNode.class);

    /**
     * 定义根节点的类型和数据
     */
    public RootMetricNode() {
        super(MetricType.ROOT, null);
        LOGGER.debug("Start init root metric.");
    }

    /**
     * 指标树初始化方法
     *
     * @param monitorHostDTO 初始化各指标的参数
     */
    public void initTree(MonitorHostDTO monitorHostDTO) {
        initMetric(MetricInstanceLoader.getMetrics(), monitorHostDTO);
    }

    @Override
    public boolean canDisplay(MonitorHostDTO monitorHostDTO) {
        return true;
    }
}
