/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.oraclepool;

import com.huawei.hercules.service.influxdb.measurement.MemoryPoolInfluxEntity;
import com.influxdb.annotations.Measurement;

/**
 * Code cache类型Oracle memory pool Influxdb持久化实体
 */
@Measurement(name = "oracle_pool_code_cache")
public class CodeCacheInfluxEntity extends MemoryPoolInfluxEntity {
}
