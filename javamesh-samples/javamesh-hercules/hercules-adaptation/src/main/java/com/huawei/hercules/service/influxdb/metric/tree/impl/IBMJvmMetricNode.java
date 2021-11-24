/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree.impl;

import com.huawei.hercules.service.influxdb.metric.tree.BaseJvmMetricNode;
import com.huawei.hercules.service.influxdb.metric.tree.JvmType;
import com.huawei.hercules.service.influxdb.metric.tree.MetricType;

/**
 * 功能描述：IBM类型的指标
 *
 * @author z30009938
 * @since 2021-11-23
 */
public class IBMJvmMetricNode extends BaseJvmMetricNode {
    public IBMJvmMetricNode() {
        super(MetricType.IBM, MetricType.ROOT, JvmType.IBM);
    }
}
