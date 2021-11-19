/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final List<IMetric> metrics = new ArrayList<>();

    static {
        ServiceLoader<IMetric> serviceLoader = ServiceLoader.load(IMetric.class);
        for (IMetric metric : serviceLoader) {
            metrics.add(metric);
        }
    }

    /**
     * 获取指定类型的metric查询服务
     *
     * @return metric查询服务
     */
    public static List<IMetric> getMetrics() {
        return metrics;
    }
}
