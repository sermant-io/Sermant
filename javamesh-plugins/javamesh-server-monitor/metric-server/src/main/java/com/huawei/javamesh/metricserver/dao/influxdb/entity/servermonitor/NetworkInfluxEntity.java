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

package com.huawei.javamesh.metricserver.dao.influxdb.entity.servermonitor;

import com.huawei.javamesh.metricserver.dao.influxdb.entity.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * server monitor network Influxdb持久化实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Measurement(name = "server_monitor_network")
public class NetworkInfluxEntity extends CommonMetricInfluxEntity {
    /**
     * 采集周期内的每秒读字节数
     */
    @Column(name = "read_bytes_per_second")
    private Long readBytesPerSec;

    /**
     * 采集周期内的每秒写字节数
     */
    @Column(name = "write_bytes_per_second")
    private Long writeBytesPerSec;

    /**
     * 采集周期内的每秒读包数
     */
    @Column(name = "read_packages_per_second")
    private Long readPackagesPerSec;

    /**
     * 采集周期内的每秒写包数
     */
    @Column(name = "write_packages_per_second")
    private Long writePackagesPerSec;
}
