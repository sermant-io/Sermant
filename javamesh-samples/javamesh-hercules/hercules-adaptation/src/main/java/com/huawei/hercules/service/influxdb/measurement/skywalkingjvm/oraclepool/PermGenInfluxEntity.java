/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.oraclepool;

import com.huawei.hercules.service.influxdb.measurement.MemoryPoolInfluxEntity;
import com.influxdb.annotations.Measurement;

/**
 * Perm gen类型Oracle memory pool Influxdb持久化实体
 */
@Measurement(name = "oracle_pool_perm_gen")
public class PermGenInfluxEntity extends MemoryPoolInfluxEntity {
}
