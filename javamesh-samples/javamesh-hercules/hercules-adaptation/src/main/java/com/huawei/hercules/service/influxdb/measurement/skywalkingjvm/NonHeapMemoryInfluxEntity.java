/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.measurement.skywalkingjvm;

import com.huawei.hercules.service.influxdb.measurement.MemoryPoolInfluxEntity;
import com.influxdb.annotations.Measurement;

/**
 * Oracle jvm metric non-heap memory Influxdb持久化实体
 */
@Measurement(name = "oracle_jvm_monitor_non_heap_memory")
public class NonHeapMemoryInfluxEntity extends MemoryPoolInfluxEntity {
}
