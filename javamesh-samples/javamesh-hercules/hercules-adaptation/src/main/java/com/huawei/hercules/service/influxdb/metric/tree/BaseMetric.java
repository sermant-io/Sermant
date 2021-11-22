/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree;

import com.huawei.hercules.exception.HerculesException;
import com.huawei.hercules.service.influxdb.SqlParam;
import com.huawei.hercules.service.influxdb.query.MetricQueryServiceLoader;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 功能描述：指标类型基类
 *
 * @author z30009938
 * @since 2021-11-19
 */
public class BaseMetric implements IMetric {
    /**
     * 当前指标类型
     */
    private final String type;

    /**
     * 父指标类型
     */
    private final String parentType;

    /**
     * 子指标列表
     */
    private final List<IMetric> childMetrics = new ArrayList<>();

    /**
     * 当前指标数据
     */
    private List<?> metricData;

    public BaseMetric(String type, String parentType) {
        if (StringUtils.isEmpty(type)) {
            throw new HerculesException("Metric type can not be empty.");
        }
        this.type = type;
        this.parentType = parentType;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getParentType() {
        return parentType;
    }

    @Override
    public List<IMetric> getChildMetric() {
        return childMetrics;
    }

    @Override
    public List<?> getMetricData() {
        return metricData;
    }

    @Override
    public void initMetric(List<IMetric> allMetrics, SqlParam sqlParam) {
        // 初始化当前指标数据
        if (sqlParam == null) {
            this.metricData = Collections.emptyList();
        } else {
            this.metricData = MetricQueryServiceLoader.getMetricData(type, sqlParam);
        }

        // 添加子指标
        if (allMetrics == null || allMetrics.isEmpty()) {
            return;
        }
        for (IMetric metric : allMetrics) {
            if (type.equals(metric.getParentType())) {
                childMetrics.add(metric);
                metric.initMetric(allMetrics, sqlParam);
            }
        }
    }
}
