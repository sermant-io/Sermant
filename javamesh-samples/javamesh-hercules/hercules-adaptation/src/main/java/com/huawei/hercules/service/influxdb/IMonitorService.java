/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb;

import com.huawei.hercules.controller.monitor.dto.MonitorHostDTO;
import com.huawei.hercules.service.influxdb.metric.tree.impl.RootMetricNode;

/**
 * 功能描述：监控数据查询接口
 *
 * @author z30009938
 * @since 2021-11-18
 */
public interface IMonitorService {
    /**
     * 获取监控数据
     *
     * @param monitorHostDTO 查询数据参数
     * @return 查询数据
     */
    RootMetricNode getAllMonitorData(MonitorHostDTO monitorHostDTO);
}
