/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree;

import com.huawei.hercules.service.influxdb.SqlParam;

import java.util.List;

/**
 * 功能描述：metric类型
 *
 * @author z30009938
 * @since 2021-11-19
 */
public interface IMetric {
    /**
     * 获取当前指标类型
     *
     * @return 当前指标类型
     */
    String getType();

    /**
     * 获取父指标类型
     *
     * @return 父指标类型
     */
    String getParentType();

    /**
     * 获取所有子指标
     *
     * @return 所有子指标
     */
    List<IMetric> getChildMetric();

    /**
     * 获取当前metric指标数据
     *
     * @return 当前metric指标数据
     */
    List<?> getMetricData();

    /**
     * 获取子指标数据
     *
     * @param allMetrics 所有的指标列表，用于初始化子指标
     * @param sqlParam 初始化需要用到的参数， 用于初始化指标数据
     */
    void initMetric(List<IMetric> allMetrics, SqlParam sqlParam);
}
