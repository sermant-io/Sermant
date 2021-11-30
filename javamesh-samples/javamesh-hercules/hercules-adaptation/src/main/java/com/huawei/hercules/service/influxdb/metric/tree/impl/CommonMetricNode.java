/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree.impl;

import com.huawei.hercules.controller.monitor.dto.MonitorHostDTO;
import com.huawei.hercules.exception.HerculesException;
import com.huawei.hercules.service.influxdb.metric.tree.IMetricNode;
import com.huawei.hercules.service.influxdb.metric.tree.JvmType;
import com.huawei.hercules.service.influxdb.metric.tree.MetricType;
import com.huawei.hercules.service.influxdb.query.IMetricQueryService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 功能描述：指标类型基类
 *
 * @author z30009938
 * @since 2021-11-19
 */
public class CommonMetricNode implements IMetricNode {
    /**
     * 当前指标类型
     */
    private final MetricType currentMetricType;

    /**
     * 父指标类型
     */
    private final MetricType parentMetricType;

    /**
     * 子指标列表
     */
    private final List<IMetricNode> childMetrics = new ArrayList<>();

    /**
     * 数据查询服务
     */
    private final IMetricQueryService metricQueryService;

    /**
     * 当前指标数据
     */
    private List<?> metricData;

    public CommonMetricNode(MetricType currentMetricType, MetricType parentMetricType, IMetricQueryService metricQueryService) {
        if (currentMetricType == null) {
            throw new HerculesException("Metric type can not be empty.");
        }
        this.currentMetricType = currentMetricType;
        this.parentMetricType = parentMetricType;
        this.metricQueryService = metricQueryService;
    }

    @Override
    public MetricType getCurrentMetricType() {
        return currentMetricType;
    }

    @Override
    public MetricType getParentMetricType() {
        return parentMetricType;
    }

    @Override
    public List<IMetricNode> getChildMetric() {
        return childMetrics;
    }

    @Override
    public List<?> getMetricData() {
        return metricData;
    }

    @Override
    public void initMetric(List<IMetricNode> allMetrics, MonitorHostDTO monitorHostDTO) {
        // 如果指标为数据节点，说明当前指标是一个叶子指标，需要去influxdb查询数据
        if (currentMetricType.isDataNode()) {
            // 初始化当前指标数据，查询之前先判断是否满足展示这个指标的条件
            if (!canDisplay(monitorHostDTO)) {
                this.metricData = Collections.emptyList();
                return;
            }

            this.metricData = metricQueryService.getMetricData(monitorHostDTO);
            return;
        }

        // 如果指标不为数据节点，直接初始化子节点列表
        for (IMetricNode metric : allMetrics) {
            if (currentMetricType.equals(metric.getParentMetricType())) {
                // 在指标列表中找到父子表是当前指标的指标，并添加到当前指标的下一级子指标列表中
                childMetrics.add(metric);

                // 添加之后初始化子指标，递归调用每一级子指标
                metric.initMetric(allMetrics, monitorHostDTO);
            }
        }
    }

    /**
     * 子类实现自己的数据是否需要查询和展示
     *
     * @param monitorHostDTO 判断是否展示的参数
     * @return true:查询和展示，false：不查询和展示
     */
    public boolean canDisplay(MonitorHostDTO monitorHostDTO) {
        if (monitorHostDTO == null) {
            return false;
        }
        if (currentMetricType.getJvmType() == JvmType.NONE) {
            return true;
        }
        if (!monitorHostDTO.getMonitorJvm()) {
            return false;
        }
        return currentMetricType.getJvmType().getName().equals(monitorHostDTO.getJvmType());
    }
}
