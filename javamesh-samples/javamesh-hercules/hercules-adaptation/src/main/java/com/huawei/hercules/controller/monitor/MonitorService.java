/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.controller.monitor;

import com.huawei.hercules.controller.monitor.influxdb.InfluxDBSqlBuilder;
import com.huawei.hercules.controller.monitor.influxdb.InfluxDBSqlExecutor;
import com.huawei.hercules.controller.monitor.influxdb.SqlModel;
import com.huawei.hercules.controller.monitor.influxdb.executor.ClassLoadingMeasurementExecutor;
import com.huawei.hercules.controller.monitor.influxdb.executor.CpuMeasurementExecutor;
import com.huawei.hercules.controller.monitor.influxdb.executor.GCMeasurementExecutor;
import com.huawei.hercules.controller.monitor.influxdb.executor.MemoryMeasurementExecutor;
import com.huawei.hercules.controller.monitor.influxdb.executor.MemoryPoolMeasurementExecutor;
import com.huawei.hercules.controller.monitor.influxdb.executor.ThreadMeasurementExecutor;
import com.huawei.hercules.controller.monitor.influxdb.measurement.CPUMeasurement;
import com.huawei.hercules.controller.monitor.influxdb.measurement.ClassLoadingMeasurement;
import com.huawei.hercules.controller.monitor.influxdb.measurement.GCMeasurement;
import com.huawei.hercules.controller.monitor.influxdb.measurement.MemoryMeasurement;
import com.huawei.hercules.controller.monitor.influxdb.measurement.MemoryPoolMeasurement;
import com.huawei.hercules.controller.monitor.influxdb.measurement.ThreadMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 功能描述：获取监控数据服务
 *
 * @author z30009938
 * @since 2021-11-14
 */
@Service
public class MonitorService {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorService.class);

    @Autowired
    private InfluxDBSqlBuilder influxDBSqlBuilder;

    @Value("${influxdb.bucket:default}")
    private String bucket;

    /**
     * 获取监控数据
     *
     * @param sqlModel 查询数据参数
     * @return 查询数据
     */
    public Map<String, Object> getAllMonitorData(SqlModel sqlModel) {
        if (sqlModel == null) {
            return Collections.emptyMap();
        }
        sqlModel.setBucket(bucket);
        Map<String, Object> data = new HashMap<>();
        data.put("classLoading", getClassLoadingMonitorData(sqlModel));
        data.put("cpu", getCpuMonitorData(sqlModel));
        data.put("gc", getGCMonitorData(sqlModel));
        data.put("memory", getMemoryMonitorData(sqlModel));
        data.put("memoryPool", getMemoryPoolMonitorData(sqlModel));
        data.put("thread", getThreadMonitorData(sqlModel));
        return data;
    }

    /**
     * 根据查询查询参数查询结果，然后封装到泛型列表
     *
     * @param sqlModel sql查询参数
     * @param sqlExecutor 查询结果分析器
     * @param <T> 需要的指标类型
     * @return 查询结果列表
     */
    public <T> List<T> getData(SqlModel sqlModel, InfluxDBSqlExecutor<T> sqlExecutor) {
        if (sqlModel == null || sqlExecutor == null) {
            return Collections.emptyList();
        }
        String getCpuDataSql = influxDBSqlBuilder.getNormalSql(sqlModel);
        return sqlExecutor.execute(getCpuDataSql);
    }

    /**
     * 查询cpu指标数据
     *
     * @param sqlModel 查询参数
     * @return 查询结果
     */
    public List<CPUMeasurement> getCpuMonitorData(SqlModel sqlModel) {
        if (sqlModel == null) {
            return Collections.emptyList();
        }
        sqlModel.setMeasurement("cpu");
        return getData(sqlModel, new CpuMeasurementExecutor());
    }

    /**
     * 查询classLoading指标数据
     *
     * @param sqlModel 查询参数
     * @return 查询结果
     */
    public List<ClassLoadingMeasurement> getClassLoadingMonitorData(SqlModel sqlModel) {
        if (sqlModel == null) {
            return Collections.emptyList();
        }
        sqlModel.setMeasurement("classLoading");
        return getData(sqlModel, new ClassLoadingMeasurementExecutor());
    }

    /**
     * 查询gc指标数据
     *
     * @param sqlModel 查询参数
     * @return 查询结果
     */
    public List<GCMeasurement> getGCMonitorData(SqlModel sqlModel) {
        if (sqlModel == null) {
            return Collections.emptyList();
        }
        sqlModel.setMeasurement("gc");
        return getData(sqlModel, new GCMeasurementExecutor());
    }

    /**
     * 查询memory指标数据
     *
     * @param sqlModel 查询参数
     * @return 查询结果
     */
    public List<MemoryMeasurement> getMemoryMonitorData(SqlModel sqlModel) {
        if (sqlModel == null) {
            return Collections.emptyList();
        }
        sqlModel.setMeasurement("memory");
        return getData(sqlModel, new MemoryMeasurementExecutor());
    }

    /**
     * 查询memoryPool指标数据
     *
     * @param sqlModel 查询参数
     * @return 查询结果
     */
    public List<MemoryPoolMeasurement> getMemoryPoolMonitorData(SqlModel sqlModel) {
        if (sqlModel == null) {
            return Collections.emptyList();
        }
        sqlModel.setMeasurement("memoryPool");
        return getData(sqlModel, new MemoryPoolMeasurementExecutor());
    }

    /**
     * 查询thread指标数据
     *
     * @param sqlModel 查询参数
     * @return 查询结果
     */
    public List<ThreadMeasurement> getThreadMonitorData(SqlModel sqlModel) {
        if (sqlModel == null) {
            return Collections.emptyList();
        }
        sqlModel.setMeasurement("thread");
        return getData(sqlModel, new ThreadMeasurementExecutor());
    }
}
