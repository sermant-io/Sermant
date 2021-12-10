/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.metricserver.dao.influxdb.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <a href=https://docs.influxdata.com/flux/v0.x/>Flux<a/>语句构造器
 */
public class FluxBuilder {

    private final static String PIPE_FORWARD_OPERATOR = " |> ";
    public static final String QUOTE_MARK = "\"";

    private final String bucket;
    private String start;
    private String end;

    /**
     * 目前仅支持 and 的方式
     */
    private final List<Filter> filters = new LinkedList<>();

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
        this.filters.add(Filter.newEquals("_measurement", measurement));
        return this;
    }

    public FluxBuilder addFilter(Filter filter) {
        this.filters.add(filter);
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
        Iterator<Filter> filterIterator = filters.iterator();
        if (filterIterator.hasNext()) {
            Filter firstFilter = filterIterator.next();
            fluxBuilder.append(PIPE_FORWARD_OPERATOR)
                .append("filter(fn: (r) => ")
                .append(buildFilterClause(firstFilter.getKey(),
                    firstFilter.getOperator(),
                    firstFilter.getValue()));
            while (filterIterator.hasNext()) {
                Filter filter = filterIterator.next();
                fluxBuilder.append(" and ")
                    .append(buildFilterClause(filter.getKey(),
                        filter.getOperator(),
                        filter.getValue()));
            }
            fluxBuilder.append(")");
        }
        return fluxBuilder.toString();
    }

    private String buildFilterClause(String key, Operator operator, Object value) {
        return "r." + key + operator.getSymbol()
            + (value instanceof String ? quotes((String) value) : value.toString());
    }

    private String quotes(String value) {
        return QUOTE_MARK + value + QUOTE_MARK;
    }

    @Data
    @AllArgsConstructor
    public static class Filter {
        private final String key;
        private final Object value;
        private final Operator operator;

        public static Filter newEquals(String tagName, Object tagValue) {
            return new Filter(tagName, tagValue, Operator.EQ);
        }
    }

    private enum Operator {
        EQ(" == "),
        NEQ(" != ");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }
}
