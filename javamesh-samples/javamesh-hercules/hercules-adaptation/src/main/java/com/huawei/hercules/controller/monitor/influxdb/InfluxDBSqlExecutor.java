/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 功能描述：执行sql，返回执行的类型列表数据
 *
 * @author z30009938
 * @since 2021-11-14
 */
@Service
public abstract class InfluxDBSqlExecutor<T> {

    @Autowired
    private InfluxDBClient influxDBClient;

    private QueryApi queryApi;

    @PostConstruct
    public void init() {
        this.queryApi = influxDBClient.getQueryApi();
    }

    /**
     * 执行sql，返回泛型指定类型的列表数据
     *
     * @param sql sql语句
     * @return 列表集合
     */
    public List<T> execute(String sql) {
        if (StringUtils.isEmpty(sql)) {
            return Collections.emptyList();
        }
        List<FluxTable> queryResult = queryApi.query(sql);
        List<T> data = new ArrayList<>();
        for (FluxTable fluxTable : queryResult) {
            for (FluxRecord record : fluxTable.getRecords()) {
                data.add(handleRecord(record));
            }
        }
        return data;
    }

    /**
     * 把结果封装到记录里面
     *
     * @param record 查询到的记录
     * @return 封装结果
     */
    protected abstract T handleRecord(FluxRecord record);
}
