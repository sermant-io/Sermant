/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.query;

import com.huawei.hercules.controller.monitor.dto.MonitorHostDTO;
import com.huawei.hercules.service.influxdb.metric.tree.MetricType;

import java.util.List;

/**
 * 功能描述：指标查询服务
 *
 * @author z30009938
 * @since 2021-11-18
 */
public interface IMetricQueryService {
    /**
     * 获取指标类型
     *
     * @return 指标类型
     */
    MetricType getMetricType();

    /**
     * 获取指标数据
     *
     * @param monitorHostDTO 查询参数
     * @return 指定类型的数据列表
     */
    List<?> getMetricData(MonitorHostDTO monitorHostDTO);
}
