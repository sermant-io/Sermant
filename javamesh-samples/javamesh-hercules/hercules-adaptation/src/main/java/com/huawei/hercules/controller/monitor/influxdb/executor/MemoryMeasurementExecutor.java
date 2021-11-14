/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor.influxdb.executor;

import com.huawei.hercules.controller.monitor.influxdb.InfluxDBSqlExecutor;
import com.huawei.hercules.controller.monitor.influxdb.measurement.MemoryMeasurement;
import com.influxdb.query.FluxRecord;

/**
 * 功能描述：
 *
 * @author z30009938
 * @since 2021-11-14
 */
public class MemoryMeasurementExecutor extends InfluxDBSqlExecutor<MemoryMeasurement> {
    @Override
    protected MemoryMeasurement handleRecord(FluxRecord record) {
        return null;
    }
}
