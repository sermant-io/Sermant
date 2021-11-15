/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.metricserver.service;

import com.huawei.javamesh.metricserver.dao.influxdb.InfluxDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

/**
 * 通用服务
 */
@Service
public class CommonService {

    private final InfluxDao influxDao;

    @Autowired
    public CommonService(InfluxDao influxDao) {
        this.influxDao = influxDao;
    }

    /**
     * 删除指定时间段的数据，谨慎使用
     * @param start 开始时间
     * @param stop 结束时间
     */
    public void delete(OffsetDateTime start, OffsetDateTime stop) {
        influxDao.delete(start, stop);
    }
}
