/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree.impl;

import com.huawei.hercules.controller.monitor.dto.MonitorHostDTO;
import com.huawei.hercules.service.influxdb.metric.tree.BaseMetricNode;
import com.huawei.hercules.service.influxdb.metric.tree.MetricType;

/**
 * 功能描述：服务器指标
 *
 * @author z30009938
 * @since 2021-11-22
 */
public class ServerMetricNode extends BaseMetricNode {
    public ServerMetricNode() {
        super(MetricType.SERVER, MetricType.ROOT);
    }

    @Override
    public boolean canDisplay(MonitorHostDTO monitorHostDTO) {
        return true;
    }
}
