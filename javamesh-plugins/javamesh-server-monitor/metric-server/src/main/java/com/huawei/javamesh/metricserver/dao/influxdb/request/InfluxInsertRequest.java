/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.request;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Influx DB 新增请求类
 */
@Data
@Builder
public class InfluxInsertRequest {
    private String measurement;
    private Instant time;
    private Map<String, String> tags;
    private Map<String, Object> fields;
}
