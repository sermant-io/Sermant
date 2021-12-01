/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.query;

import com.huawei.hercules.service.influxdb.metric.tree.MetricType;
import com.huawei.hercules.service.influxdb.query.impl.CommonMetricQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：Metric查询服务加载器，通过SPI机制把所有的查询服务加载到系统
 *
 * @author z30009938
 * @since 2021-11-19
 */
@Service
public class MetricQueryServiceLoader {
    /**
     * 保存所有Metric服务
     */
    private final Map<MetricType, IMetricQueryService> metricQueryServices = new HashMap<>();

    @Autowired
    private InfluxDBSqlExecutor influxDBSqlExecutor;

    @PostConstruct
    public void init() {
        for (MetricType metricType : MetricType.values()) {
            CommonMetricQueryService metricQueryService = new CommonMetricQueryService(metricType, influxDBSqlExecutor);
            metricQueryServices.put(metricType, metricQueryService);
        }
    }

    /**
     * 获取指定类型的metric查询服务
     *
     * @param metricType metric类型
     * @return metric查询服务
     */
    public IMetricQueryService getMetricQueryService(MetricType metricType) {
        return metricQueryServices.get(metricType);
    }
}
