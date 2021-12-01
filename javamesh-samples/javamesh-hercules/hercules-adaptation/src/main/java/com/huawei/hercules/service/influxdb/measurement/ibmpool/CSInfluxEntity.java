/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.measurement.ibmpool;

import com.huawei.hercules.service.influxdb.measurement.MemoryPoolInfluxEntity;
import com.influxdb.annotations.Measurement;

/**
 * Class storage类型IBM memory pool Influxdb持久化实体
 */
@Measurement(name = "ibm_pool_cs")
public class CSInfluxEntity extends MemoryPoolInfluxEntity {
}
