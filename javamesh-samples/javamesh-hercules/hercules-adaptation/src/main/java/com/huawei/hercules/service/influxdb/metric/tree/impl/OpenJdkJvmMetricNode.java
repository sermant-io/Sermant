/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree.impl;

import com.huawei.hercules.service.influxdb.metric.tree.BaseJvmMetricNode;
import com.huawei.hercules.service.influxdb.metric.tree.JvmType;
import com.huawei.hercules.service.influxdb.metric.tree.MetricType;

/**
 * 功能描述：OpenJdk类型指标
 *
 * @author z30009938
 * @since 2021-11-23
 */
public class OpenJdkJvmMetricNode extends BaseJvmMetricNode {
    /**
     * 供子类调用
     */
    public OpenJdkJvmMetricNode() {
        super(MetricType.OPEN_JDK, MetricType.ROOT, JvmType.OPEN_JDK);
    }
}
