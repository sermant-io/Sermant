/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.hercules.service.influxdb.query;

import com.huawei.hercules.service.influxdb.metric.tree.MetricType;
import com.huawei.hercules.service.influxdb.query.impl.CommonMetricQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：Metric查询服务加载器，通过SPI机制把所有的查询服务加载到系统
 *
 * @author z30009938
 * @since 2021-11-19
 */
@Service
public class MetricQueryServiceLoader {
    /**
     * 保存所有Metric服务
     */
    private final Map<MetricType, IMetricQueryService> metricQueryServices = new HashMap<>();

    @Autowired
    private InfluxDBSqlExecutor influxDBSqlExecutor;

    @PostConstruct
    public void init() {
        for (MetricType metricType : MetricType.values()) {
            CommonMetricQueryService metricQueryService = new CommonMetricQueryService(metricType, influxDBSqlExecutor);
            metricQueryServices.put(metricType, metricQueryService);
        }
    }

    /**
     * 获取指定类型的metric查询服务
     *
     * @param metricType metric类型
     * @return metric查询服务
     */
    public IMetricQueryService getMetricQueryService(MetricType metricType) {
        return metricQueryServices.get(metricType);
    }
}
