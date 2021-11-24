/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree.impl;

import com.huawei.hercules.service.influxdb.metric.tree.BaseJvmMetricNode;
import com.huawei.hercules.service.influxdb.metric.tree.JvmType;
import com.huawei.hercules.service.influxdb.metric.tree.MetricType;

/**
 * 功能描述：IBM中cs
 *
 * @author z30009938
 * @since 2021-11-23
 */
public class IBMJvmCSMetricNode extends BaseJvmMetricNode {
    public IBMJvmCSMetricNode() {
        super(MetricType.IBM_CS, MetricType.IBM, JvmType.IBM);
    }
}
