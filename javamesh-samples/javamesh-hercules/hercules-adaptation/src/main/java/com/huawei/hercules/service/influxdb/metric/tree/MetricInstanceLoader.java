/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 功能描述：metric加载器
 *
 * @author z30009938
 * @since 2021-11-19
 */
public class MetricInstanceLoader {
    /**
     * 保存所有Metric服务
     */
    private static final List<IMetricNode> metrics = new ArrayList<>();

    static {
        ServiceLoader<IMetricNode> serviceLoader = ServiceLoader.load(IMetricNode.class);
        for (IMetricNode metric : serviceLoader) {
            metrics.add(metric);
        }
    }

    /**
     * 获取指定类型的metric查询服务
     *
     * @return metric查询服务
     */
    public static List<IMetricNode> getMetrics() {
        return metrics;
    }
}
