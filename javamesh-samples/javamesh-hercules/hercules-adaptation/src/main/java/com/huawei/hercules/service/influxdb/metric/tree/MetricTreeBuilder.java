/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree;

import com.huawei.hercules.controller.monitor.dto.MonitorHostDTO;
import com.huawei.hercules.service.influxdb.metric.tree.impl.CommonMetricNode;
import com.huawei.hercules.service.influxdb.query.MetricQueryServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述：metric加载器
 *
 * @author z30009938
 * @since 2021-11-19
 */
@Service
public class MetricTreeBuilder {
    /**
     * 日志工具
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricTreeBuilder.class);

    @Autowired
    private MetricQueryServiceLoader metricQueryServiceLoader;

    /**
     * 保存所有Metric服务
     */
    private final List<IMetricNode> metrics = new ArrayList<>();

    @PostConstruct
    public void init() {
        for (MetricType metricType : MetricType.values()) {
            CommonMetricNode metricNode = new CommonMetricNode(
                    metricType,
                    metricType.getParentMetricType(),
                    metricQueryServiceLoader.getMetricQueryService(metricType)
            );
            LOGGER.info("Add metric success:{}", metricType.getName());
            metrics.add(metricNode);
        }
    }

    /**
     * 获取指定类型的metric查询服务
     *
     * @return metric查询服务
     */
    public IMetricNode buildMetricTree(MonitorHostDTO monitorHostDTO) {
        CommonMetricNode rootMetricNode = new CommonMetricNode(MetricType.ROOT, null, null);
        rootMetricNode.initMetric(metrics, monitorHostDTO);
        return rootMetricNode;
    }
}
