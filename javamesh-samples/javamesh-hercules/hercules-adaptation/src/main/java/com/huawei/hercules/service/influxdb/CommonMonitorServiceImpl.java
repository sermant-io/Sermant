/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb;

import com.huawei.hercules.controller.monitor.dto.MonitorHostDTO;
import com.huawei.hercules.service.influxdb.metric.tree.impl.RootMetricNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 功能描述：获取监控数据服务
 *
 * @author z30009938
 * @since 2021-11-14
 */
@Service
public class CommonMonitorServiceImpl implements IMonitorService {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonMonitorServiceImpl.class);

    /**
     * 获取监控数据
     *
     * @param monitorHostDTO 查询数据参数
     * @return 查询数据
     */
    @Override
    public RootMetricNode getAllMonitorData(MonitorHostDTO monitorHostDTO) {
        LOGGER.debug("Start init metric, param:{}", monitorHostDTO);
        RootMetricNode rootMetric = new RootMetricNode();
        rootMetric.initTree(monitorHostDTO);
        return rootMetric;
    }
}
