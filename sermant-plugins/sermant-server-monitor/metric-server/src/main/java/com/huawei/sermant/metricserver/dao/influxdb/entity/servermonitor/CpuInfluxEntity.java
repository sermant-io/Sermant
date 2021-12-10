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

package com.huawei.sermant.metricserver.dao.influxdb.entity.servermonitor;

import com.huawei.sermant.metricserver.dao.influxdb.entity.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * server monitor cpu Influxdb持久化实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Measurement(name = "server_monitor_cpu")
public class CpuInfluxEntity extends CommonMetricInfluxEntity {
    /**
     * idle时间百分占比
     */
    @Column(name = "idle_percentage")
    private Long idlePercentage;

    /**
     * io wait时间百分占比
     */
    @Column(name = "io_wait_percentage")
    private Long ioWaitPercentage;

    /**
     * sys时间百分占比
     */
    @Column(name = "sys_percentage")
    private Long sysPercentage;

    /**
     * user和nice时间百分占比
     */
    @Column(name = "user_percentage")
    private Long userPercentage;
}
