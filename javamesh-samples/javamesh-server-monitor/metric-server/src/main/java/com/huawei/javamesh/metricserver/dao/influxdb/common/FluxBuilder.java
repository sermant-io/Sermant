/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.dao.influxdb.common;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <a href=https://docs.influxdata.com/flux/v0.x/>Flux<a/>语句构造器
 */
public class FluxBuilder {

    private final static String PIPE_FORWARD_OPERATOR = " |> ";

    private final String bucket;
    private String start;
    private String end;
    private String measurement;

    private FluxBuilder(String bucket) {
        this.bucket = bucket;
    }

    public static FluxBuilder from(String bucket) {
        Assert.hasText(bucket, "Bucket must not be blank.");
        return new FluxBuilder(bucket);
    }

    public FluxBuilder range(String start) {
        this.start = start;
        return this;
    }

    public FluxBuilder range(String start, String end) {
        this.start = start;
        this.end = end;
        return this;
    }

    public FluxBuilder measurement(String measurement) {
        this.measurement = measurement;
        return this;
    }

    public String build() {
        final StringBuilder fluxBuilder = new StringBuilder("from(bucket:\"" + bucket + "\")");
        if (StringUtils.hasText(start)) {
            fluxBuilder.append(PIPE_FORWARD_OPERATOR)
                .append("range(start: ").append(start);
            if (StringUtils.hasText(end)) {
                fluxBuilder.append(", end: ").append(end);
            }
            fluxBuilder.append(")");
        }
        if (StringUtils.hasText(measurement)) {
            fluxBuilder.append(PIPE_FORWARD_OPERATOR)
                .append("filter(fn: (r) => r._measurement == \"")
                .append(measurement)
                .append("\")");
        }
        return fluxBuilder.toString();
    }
}
