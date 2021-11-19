/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
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
 * @author z30009938
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

    /**
     * global influxdb bucket
     */
    @Value("${influxdb.bucket:default}")
    private String bucket;

    /**
     * SQL模板
     */
    private final String baseSql = String.format(Locale.ENGLISH, "from(bucket:\"%s\") ", bucket);

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
        return queryApi.query(baseSql + conditionSql, measurementType);
    }
}
