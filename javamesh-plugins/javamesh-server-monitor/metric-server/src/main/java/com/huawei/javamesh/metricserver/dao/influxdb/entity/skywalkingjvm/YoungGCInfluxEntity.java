/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.entity.skywalkingjvm;

import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Oracle jvm metric young gc Influxdb持久化实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Measurement(name = "oracle_jvm_monitor_young_gc")
public class YoungGCInfluxEntity extends GCInfluxEntity {
}
