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

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 功能描述：执行sql，返回执行的类型列表数据
 *
 * 
 * @since 2021-11-14
 */
@Service
public class InfluxDBSqlExecutor {
    /**
     * influxdb client
     */
    @Autowired
    private InfluxDBClient influxDBClient;

    /**
     * query api for influxdb
     */
    private QueryApi queryApi;

    @PostConstruct
    public void init() {
        this.queryApi = influxDBClient.getQueryApi();
    }

    /**
     * 执行sql，返回泛型指定类型的列表数据
     *
     * @param conditionSql sql语句
     * @return 列表集合
     */
    public List<?> execute(String conditionSql, Class<?> measurementType) {
        if (StringUtils.isEmpty(conditionSql) || measurementType == null) {
            return Collections.emptyList();
        }
        return queryApi.query(conditionSql, measurementType);
    }
}
