/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.measurement.skywalkingjvm;

import com.influxdb.annotations.Measurement;

/**
 * Oracle jvm metric old gc Influxdb持久化实体
 */
@Measurement(name = "oracle_jvm_monitor_old_gc")
public class OldGCInfluxEntity extends GCInfluxEntity {
}
