/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.query;

import com.huawei.hercules.exception.HerculesException;
import com.huawei.hercules.service.influxdb.SqlParam;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 功能描述：query metric base class
 *
 * @author z30009938
 * @since 2021-11-19
 */
public abstract class BaseMetricQueryService implements IMetricQueryService {
    /**
     * metric type
     */
    private final String metricType;

    /**
     * sql of query executor
     */
    private final InfluxDBSqlExecutor influxDBSqlExecutor;

    /**
     * init metric type and child metric
     *
     * @param metricType this metric type
     */
    public BaseMetricQueryService(String metricType, InfluxDBSqlExecutor influxDBSqlExecutor) {
        if (StringUtils.isEmpty(metricType)) {
            throw new HerculesException("Metric type can not be empty.");
        }
        if (influxDBSqlExecutor == null) {
            throw new HerculesException("Query executor is empty.");
        }
        this.influxDBSqlExecutor = influxDBSqlExecutor;
        this.metricType = metricType;
    }

    @Override
    public String getMetricType() {
        return metricType;
    }

    @Override
    public List<?> getMetricData(SqlParam sqlParam) {
        return influxDBSqlExecutor.execute(buildSql(sqlParam), getDataType());
    }

    /**
     * build metric query data
     *
     * @param sqlParam query param
     * @return sql
     */
    public abstract String buildSql(SqlParam sqlParam);

    /**
     * get the java bean type of data
     *
     * @return the java bean type of data
     */
    public abstract Class<?> getDataType();
}
