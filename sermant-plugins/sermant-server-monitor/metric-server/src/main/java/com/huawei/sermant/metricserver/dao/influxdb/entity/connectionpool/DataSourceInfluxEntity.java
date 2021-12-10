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

package com.huawei.sermant.metricserver.dao.influxdb.entity.connectionpool;

import com.huawei.sermant.metricserver.dao.influxdb.entity.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Druid datasource Influxdb持久化实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Measurement(name = "druid_datasource")
public class DataSourceInfluxEntity extends CommonMetricInfluxEntity {
    @Column(tag = true, name = "name")
    private String name;

    @Column(tag = true, name = "database_peer")
    private String databasePeer;

    @Column(name = "active_count")
    private Long activeCount;

    @Column(name = "pooling_count")
    private Long poolingCount;

    @Column(name = "max_active")
    private Long maxActive;

    @Column(name = "initial_size")
    private Long initialSize;
}
