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

package com.huawei.sermant.metricserver.dao.influxdb.request;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Influx DB 查询请求类
 */
@Data
@Builder
public class InfluxQueryRequest {
    private String measurement;

    /**
     * Start and stop values can be relative using negative durations or absolute using timestamps.
     *
     * <p>Example relative time ranges:<p/>
     * <p>  Relative time range with start only. Stop defaults to now.
     * from(bucket:"example-bucket")
     * |> range(start: -1h)<p/>
     *
     * <p>  Relative time range with start and stop
     * from(bucket:"example-bucket")
     * |> range(start: -1h, stop: -10m)<p/>
     *
     *
     * <p>Example absolute time range:<p/>
     * <p>  from(bucket:"example-bucket")
     * |> range(start: 2021-01-01T00:00:00Z, stop: 2021-01-01T12:00:00Z)<p/>
     */
    private String start;
    private String end;

    /**
     * tag筛选条件
     * <p>
     * 仅支持tag1==value1 and tag2==value2 ...这种简单的与逻辑
     */
    private Map<String, String> tags;
}
