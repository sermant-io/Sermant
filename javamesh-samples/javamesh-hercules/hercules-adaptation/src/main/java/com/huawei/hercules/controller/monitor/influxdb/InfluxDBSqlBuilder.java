/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor.influxdb;

import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * 功能描述：InfluxDBSql语句
 *
 * @author z30009938
 * @since 2021-11-14
 */
@Service
public class InfluxDBSqlBuilder {
    /**
     * SQL模板
     */
    public static final String SQL_TEMPLATE = "from(bucket:\"%s\") " +
            "|> range(start: %s, stop: %s) " +
            "|> filter(fn: (r) => r._measurement == \"%s\" and r.ip = \"%s\")";

    /**
     * 获取常规数据sql
     *
     * @param sqlModel sql参数
     * @return sql语句
     */
    public String getNormalSql(SqlModel sqlModel) {
        return String.format(Locale.ENGLISH,
                SQL_TEMPLATE,
                sqlModel.getBucket(),
                sqlModel.getStartTime(),
                sqlModel.getEndTime(),
                sqlModel.getMeasurement(),
                sqlModel.getIp());
    }
}
