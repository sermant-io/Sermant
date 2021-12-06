/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.hercules.service.influxdb.measurement.openjdk;

import com.huawei.hercules.service.influxdb.measurement.CommonMetricInfluxEntity;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;

/**
 * Oracle jvm metric thread Influxdb持久化实体
 */
@Measurement(name = "oracle_jvm_monitor_thread")
public class ThreadInfluxEntity extends CommonMetricInfluxEntity {

    @Column(name = "live_count")
    private Long liveCount;

    @Column(name = "daemon_count")
    private Long daemonCount;

    @Column(name = "peak_count")
    private Long peakCount;
}
