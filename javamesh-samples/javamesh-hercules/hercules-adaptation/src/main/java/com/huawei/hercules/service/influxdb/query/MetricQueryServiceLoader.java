/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.query;

import com.huawei.hercules.controller.monitor.dto.MonitorHostDTO;
import com.huawei.hercules.service.influxdb.metric.tree.MetricType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * 功能描述：Metric查询服务加载器，通过SPI机制把所有的查询服务加载到系统
 *
 * @author z30009938
 * @since 2021-11-19
 */
public class MetricQueryServiceLoader {
    /**
     * 保存所有Metric服务
     */
    private static final Map<MetricType, IMetricQueryService> metricQueryServices = new HashMap<>();

    static {
        ServiceLoader<IMetricQueryService> serviceLoader = ServiceLoader.load(IMetricQueryService.class);
        for (IMetricQueryService metricQueryService : serviceLoader) {
            metricQueryServices.put(metricQueryService.getMetricType(), metricQueryService);
        }
    }

    /**
     * 获取指定类型的metric查询服务
     *
     * @param metricType metric类型
     * @return metric查询服务
     */
    public static IMetricQueryService getMetricQueryService(MetricType metricType) {
        return metricQueryServices.get(metricType);
    }

    /**
     * 查询数据
     *
     * @param metricType 指标类型
     * @param monitorHostDTO   查询参数
     * @return 查询结果
     */
    public static List<?> getMetricData(MetricType metricType, MonitorHostDTO monitorHostDTO) {
        IMetricQueryService metricQueryService = metricQueryServices.get(metricType);
        return metricQueryService.getMetricData(monitorHostDTO);
    }
}
