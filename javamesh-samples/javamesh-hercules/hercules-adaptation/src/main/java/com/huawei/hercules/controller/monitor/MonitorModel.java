/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor;

import com.huawei.hercules.service.influxdb.metric.tree.impl.RootMetric;

import java.util.Map;

/**
 * 功能描述：查询结果对象封装
 *
 * @author z30009938
 * @since 2021-11-12
 */
public class MonitorModel {
    /**
     * 查询结果是否成功
     */
    private boolean success;

    /**
     * 查询数据
     */
    private RootMetric data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public RootMetric getData() {
        return data;
    }

    public void setData(RootMetric data) {
        this.data = data;
    }
}
