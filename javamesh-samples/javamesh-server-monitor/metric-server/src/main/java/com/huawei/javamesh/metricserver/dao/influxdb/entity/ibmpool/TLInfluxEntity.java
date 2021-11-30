/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.entity.ibmpool;

import com.huawei.javamesh.metricserver.dao.influxdb.entity.MemoryPoolInfluxEntity;
import com.influxdb.annotations.Measurement;

/**
 * Tenured-LOA类型IBM memory pool Influxdb持久化实体
 */
@Measurement(name = "ibm_pool_tl")
public class TLInfluxEntity extends MemoryPoolInfluxEntity {
}
