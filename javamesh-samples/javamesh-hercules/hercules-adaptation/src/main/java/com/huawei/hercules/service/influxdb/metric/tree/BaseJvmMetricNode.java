/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree;

import com.huawei.hercules.controller.monitor.dto.MonitorHostDTO;
import com.huawei.hercules.exception.HerculesException;

/**
 * 功能描述：JVM指标基类
 *
 * @author z30009938
 * @since 2021-11-22
 */
public class BaseJvmMetricNode extends BaseMetricNode {
    /**
     * jvm指标中的jvm类型
     */
    private final JvmType jvmType;

    /**
     * 供子类调用
     *
     * @param currentMetricType 指标类型
     * @param parentType        指标父类类型
     */
    public BaseJvmMetricNode(MetricType currentMetricType, MetricType parentType, JvmType jvmType) {
        super(currentMetricType, parentType);
        if (jvmType == null) {
            throw new HerculesException("Jvm type of metric is null.");
        }
        this.jvmType = jvmType;
    }

    @Override
    public boolean canDisplay(MonitorHostDTO monitorHostDTO) {
        if (monitorHostDTO == null) {
            return false;
        }
        if (!monitorHostDTO.getMonitorJvm()) {
            return false;
        }
        return jvmType.getName().equals(monitorHostDTO.getJvmType());
    }
}
