/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.oap.query.graphql;

import com.huawei.apm.core.query.ConnectionsCountMetric;
import com.huawei.apm.core.query.DruidQueryCondition;
import com.huawei.apm.core.query.NodeCondition;
import com.huawei.apm.core.query.NodeDuration;
import com.huawei.apm.core.query.NodeRecord;
import com.huawei.apm.core.query.NodeRecords;
import com.huawei.apm.core.source.ConnectionPoolMetric;
import com.huawei.skywalking.oap.server.receiver.connection.pool.module.ConnectionPoolModule;
import com.huawei.skywalking.oap.server.receiver.connection.pool.service.DruidQueryService;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;

import org.apache.commons.lang3.StringUtils;
import org.apache.skywalking.apm.util.StringUtil;
import org.apache.skywalking.oap.query.graphql.GraphQLQueryConfig;
import org.apache.skywalking.oap.server.core.Const;
import org.apache.skywalking.oap.server.core.analysis.IDManager;
import org.apache.skywalking.oap.server.core.analysis.NodeType;
import org.apache.skywalking.oap.server.core.analysis.TimeBucket;
import org.apache.skywalking.oap.server.core.query.input.Duration;
import org.apache.skywalking.oap.server.library.module.ModuleManager;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 提供查询数据库指标的接口
 *
 * @author zhouss
 * @since 2020-11-26
 */
public class DruidQuery implements GraphQLQueryResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DruidQuery.class);

    private final ModuleManager moduleManager;

    private final GraphQLQueryConfig config;

    /**
     * 应用指标名称
     */
    private final String appMetric = "druid_app_active_count";

    /**
     * 实例指标名称
     */
    private final String instanceMetric = "druid_instance_active_count";

    /**
     * 数据源指标名称
     */
    private final String datasourceMetric = "druid_datasource_active_count";

    /**
     * 定义将秒转换分钟
     * skywalking原本设计分钟转换为秒是 sec * 100 = minute
     */
    private final int divisorForConvertSecToMinute = 100;

    private DruidQueryService druidQueryService;

    public DruidQuery(ModuleManager moduleManager, GraphQLQueryConfig conifg) {
        this.moduleManager = moduleManager;
        this.config = conifg;
    }

    private DruidQueryService getDruidQueryService() {
        if (druidQueryService == null) {
            this.druidQueryService =
                    moduleManager.find(ConnectionPoolModule.NAME).provider().getService(DruidQueryService.class);
        }
        return druidQueryService;
    }

    /**
     * query data source by instance name
     *
     * @param instanceName original instance name of skywalking
     * @param duration     which duration to query , it has three steps : DAY, MINUTE, HOUR
     * @param condition    condition for pageSize, pageNum, keyword
     * @return NodeRecords  datasource records
     * @throws IOException if storage is mysql, it is sql execute Exception
     */
    public NodeRecords queryDataSources(String instanceName, DruidQueryCondition condition, Duration duration)
            throws IOException {
        NodeDuration nodeDuration = getDuration(duration);
        NodeRecords nodeRecords =
                getDruidQueryService().queryNodeRecords(
                        this.buildNodeCondition(
                                nodeDuration,
                                condition,
                                datasourceMetric,
                                ConnectionPoolMetric.SERVICE_ID,
                                Arrays.asList(buildServiceId(instanceName))));
        return nodeRecords;
    }

    /**
     * query ip instances of appName
     *
     * @param appName   original skywalking app name
     * @param duration  duration which duration to query , it has three steps : DAY, MINUTE, HOUR
     * @param condition condition for pageSize, pageNum, keyword
     * @return NodeRecords  instance records
     * @throws IOException if storage is mysql, it is sql execute Exception
     */
    public NodeRecords queryInstances(String appName, DruidQueryCondition condition, Duration duration)
            throws IOException {
        return queryInstanceRecords(condition, Arrays.asList(buildServiceId(appName)), ConnectionPoolMetric.SERVICE_ID,
                duration);
    }

    /**
     * query apps which is assigned in the duration
     *
     * @param duration  duration duration which duration to query , it has three steps : DAY, MINUTE, HOUR
     * @param condition condition for pageSize, pageNum, keyword
     * @return NodeRecords  APP records
     * @throws IOException if storage is mysql, it is sql execute Exception
     */
    public NodeRecords queryApps(DruidQueryCondition condition, Duration duration) throws IOException {
        NodeDuration nodeDuration = getDuration(duration);
        String databasePeer = condition.getDatabasePeer();

        // 应用通过过滤统计相关连接数
        NodeRecords appRecords =
                getDruidQueryService()
                        .queryNodeRecords(
                                NodeCondition.builder()
                                        .startTime(nodeDuration.getStart())
                                        .endTime(nodeDuration.getEnd())
                                        .metricName(appMetric)
                                        .nameKeyWord(condition.getNameKeyword())
                                        .build());
        List<NodeRecord> nodeRecords = appRecords.getRecords();
        if (CollectionUtils.isEmpty(nodeRecords)) {
            return appRecords;
        }

        // 查询app的关联的实例列表
        List<NodeRecord> instanceRecords =
                queryInstanceRecords(null,
                        nodeRecords.stream()
                                .map(record -> buildServiceId(record.getName()))
                                .collect(Collectors.toList()), ConnectionPoolMetric.SERVICE_ID, duration).getRecords();
        if (CollectionUtils.isEmpty(instanceRecords)) {
            // 没有实例的应用，标明该应用并没有
            return new NodeRecords();
        }
        return staticsAppRecordsConnection(appRecords, instanceRecords, databasePeer);
    }

    /**
     * 通过实例列表统计应用连接数
     *
     * @param appRecords      待分页的记录
     * @param instanceRecords 实例列表
     * @param databasePeer    数据库实例
     * @return 分页后的记录
     */
    private NodeRecords staticsAppRecordsConnection(
            NodeRecords appRecords, List<NodeRecord> instanceRecords, String databasePeer) {
        // 实例根据serviceId 分组
        Map<String, List<NodeRecord>> instanceMap =
                instanceRecords.stream()
                        .filter(record -> StringUtils.contains(record.getDatabasePeers(), databasePeer))
                        .collect(Collectors.groupingBy(NodeRecord::getServiceId));

        // 过滤出有实例节点的应用记录,并统计连接数
        List<NodeRecord> filteredRecords =
                appRecords.getRecords().stream()
                        .filter(record -> instanceMap.containsKey(buildServiceId(record.getName())))
                        .map(record -> {
                            List<NodeRecord> instances = instanceMap.get(buildServiceId(record.getName()));
                            if (CollectionUtils.isEmpty(instances)) {
                                return record;
                            }
                            return calculateAppRecordConnections(instances, databasePeer, record);
                        })
                        .collect(Collectors.toList());
        appRecords.setRecords(filteredRecords);
        appRecords.setTotal(filteredRecords.size());
        return appRecords;
    }

    /**
     * 转换查询时间段 将时间类型转换为分钟级别
     * 如果前端没有传查询时间段  默认使用配置的区间 <h1>alive_time</h1>  单位分钟
     *
     * @param duration 时段
     * @return 转换后的时段
     */
    private NodeDuration getDuration(Duration duration) {
        long start;
        long end;
        if (duration == null) {
            LOGGER.debug("query duration is null, use the default config, the alive time is {} seconds",
                    config.getAliveTime());
            end = TimeBucket.getMinuteTimeBucket(System.currentTimeMillis());
            start = end - config.getAliveTime();
        } else {
            // 将时间格式由yyyyMMddHHmmss 转换为 yyyyMMddHHmm
            start = duration.getStartTimeBucketInSec() / divisorForConvertSecToMinute;
            end = duration.getEndTimeBucketInSec() / divisorForConvertSecToMinute;
        }
        return NodeDuration.builder().start(start).end(end).build();
    }

    /**
     * 构建查询条件
     *
     * @param nodeDuration 查询时间段
     * @param condition    前端查询条件
     * @param metricName   指标名称
     * @param columnName   查询字段名
     * @param columnValues 查询字段值集合
     * @return 查询条件
     */
    private NodeCondition buildNodeCondition(NodeDuration nodeDuration, DruidQueryCondition condition,
                                            String metricName, String columnName, List<String> columnValues) {
        NodeCondition.NodeConditionBuilder builder =
                NodeCondition.builder()
                        .startTime(nodeDuration.getStart())
                        .endTime(nodeDuration.getEnd())
                        .metricName(metricName)
                        .groupByColumn(ConnectionPoolMetric.ENTITY_ID);
        if (!StringUtil.isEmpty(columnName) && CollectionUtils.isNotEmpty(columnValues)) {
            builder.columnName(columnName).columnValues(columnValues);
        }
        if (condition != null) {
            builder.pageNum(condition.getPageNum())
                    .pageSize(condition.getPageSize())
                    .nameKeyWord(condition.getNameKeyword())
                    .databasePeer(condition.getDatabasePeer());
        }
        return builder.build();
    }

    private String buildServiceId(String name) {
        if (StringUtil.isEmpty(name)) {
            return Const.EMPTY_STRING;
        }
        return IDManager.ServiceID.buildId(name, NodeType.Normal);
    }

    /**
     * 计算应用连接数汇总
     *
     * @param records      实例列表
     * @param databasePeer 数据库实例
     * @param record       需要汇总的记录
     * @return 统计连接数后的记录
     */
    private NodeRecord calculateAppRecordConnections(List<NodeRecord> records, String databasePeer, NodeRecord record) {
        if (CollectionUtils.isEmpty(records) || record == null) {
            return record;
        }
        if (StringUtil.isEmpty(databasePeer)) {
            // 应用的连接数数初始数据都会是0,如果没有数据库实例筛选，则直接统计所有的连接数
            records.add(0, record);
            return calculateAppRecordConnections(records);
        }
        int activeCount = 0;
        int maxActive = 0;
        int poolingCount = 0;
        for (int index = 0; index < records.size(); index++) {
            Map<String, ConnectionsCountMetric> connectionsCountMetricMap =
                    NodeRecord.convertDatabasePeers2Map(records.get(index).getDatabasePeers());
            ConnectionsCountMetric connectionsCountMetric = connectionsCountMetricMap.get(databasePeer);
            if (connectionsCountMetric != null) {
                activeCount += connectionsCountMetric.getActiveCount();
                maxActive += connectionsCountMetric.getMaxCount();
                poolingCount += connectionsCountMetric.getPoolingCount();
            }
        }
        record.setActiveCount(activeCount);
        record.setMaxActive(maxActive);
        record.setPoolingCount(poolingCount);
        return record;
    }

    /**
     * 汇总集合的各项连接数指标
     *
     * @param records 维度集合（实例，应用，数据源）
     * @return 汇总后的应用记录
     */
    private NodeRecord calculateAppRecordConnections(List<NodeRecord> records) {
        NodeRecord nodeRecord =
                records.stream()
                        .reduce((curRecord, nextRecord) -> {
                            curRecord.add(nextRecord);
                            return curRecord;
                        })
                        .get();
        return nodeRecord;
    }

    private NodeRecords queryInstanceRecords(
            DruidQueryCondition condition, List<String> columnValues, String columnName, Duration duration)
            throws IOException {
        NodeDuration nodeDuration = getDuration(duration);
        NodeRecords nodeRecords = getDruidQueryService().queryNodeRecords(this.buildNodeCondition(
                nodeDuration, condition, instanceMetric, columnName, columnValues));
        if (Objects.nonNull(condition) && StringUtil.isNotEmpty(condition.getDatabasePeer())) {
            // 将连接数指定为当前数据库实例的连接数
            nodeRecords.getRecords()
                .forEach(
                    record -> {
                        Map<String, ConnectionsCountMetric> connectionsCountMetricMap =
                                NodeRecord.convertDatabasePeers2Map(record.getDatabasePeers());
                        ConnectionsCountMetric connectionsCountMetric =
                                connectionsCountMetricMap.get(condition.getDatabasePeer());
                        if (connectionsCountMetric != null) {
                            record.setPoolingCount(connectionsCountMetric.getPoolingCount());
                            record.setMaxActive(connectionsCountMetric.getMaxCount());
                            record.setActiveCount(connectionsCountMetric.getActiveCount());
                        }
                    });
        }
        return nodeRecords;
    }
}
