/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.drill;

import com.google.protobuf.InvalidProtocolBufferException;
import com.huawei.apm.network.language.agent.v3.ConnectionPoolCollection;
import com.huawei.apm.network.language.agent.v3.ConnectionPoolMetric;
import com.huawei.apm.network.language.agent.v3.DataSourceBean;
import com.huawei.apm.network.language.agent.v3.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.huawei.apm.core.drill.MetricLabelValueType.INT;

/**
 * ConnectionPoolMetric复制器
 *
 * @author qinfurong
 * @since 2021-07-01
 */
public class ConnectionPoolMetricReplicator extends BaseReplicator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPoolMetricReplicator.class);

    /**
     * 复制ConnectionPoolMetric，并按照标签配置值修改
     *
     * @param builder 被复制的采集对象
     */
    public static void copyModification(ConnectionPoolCollection.Builder builder) {
        List<ConnectionPoolMetric.Builder> metrics = builder.getInstanceMetricsBuilderList();
        List<ConnectionPoolMetric.Builder> copyMetrics = new ArrayList<>(metrics.size());

        // 深拷贝对象
        metrics.forEach(metricBuilder -> {
            try {
                ConnectionPoolMetric connectionPoolMetric = ConnectionPoolMetric.parseFrom(metricBuilder.build().toByteArray());
                copyMetrics.add(connectionPoolMetric.toBuilder());
            } catch (InvalidProtocolBufferException e) {
                LOGGER.error("parse ConnectionPoolMetric to ConnectionPoolMetric failed!, message:{}", e);
            }
        });

        // 解析指标标签值
        Map<String, Object> metricValueMap = parseLabelMetricValueToMap(builder.getLabelMetricValue());

        copyMetrics.forEach(metricBuilder -> {
            toCopyModificationDataSourceBean(metricBuilder.getInstance(), metricBuilder.getDataSourceBeansList(), metricValueMap);
        });

        builder.clearInstanceMetrics();
        copyMetrics.forEach(metricBuilder -> builder.addInstanceMetrics(metricBuilder));
    }

    private static void toCopyModificationDataSourceBean(Instance instance, List<DataSourceBean> dataSourceBeansList, Map<String, Object> metricValueMap) {
        if (dataSourceBeansList == null || dataSourceBeansList.isEmpty()) {
            return;
        }

        // druid_instance_active_count 指标的汇总计算
        // 每一个databasePeer值汇总
        Map<String, AtomicInteger> groupActiveCounts = new HashMap<>();
        Map<String, AtomicInteger> groupPoolingCounts = new HashMap<>();
        Map<String, AtomicInteger> groupMaxActives = new HashMap<>();

        //每一次采集值汇总，设置Instance对象值
        AtomicInteger instanceActiveCount = new AtomicInteger();
        AtomicInteger instancePoolingCount = new AtomicInteger();
        AtomicInteger instanceMaxCount = new AtomicInteger();

        dataSourceBeansList.forEach(dataSourceBean -> {
            updateNumericFiledValue(dataSourceBean, "activeCount_", INT, metricValueMap.get("DRUID_DATASOURCE_ACTIVE_COUNT"));
            updateNumericFiledValue(dataSourceBean, "poolingCount_", INT, metricValueMap.get("DRUID_DATASOURCE_POOLING_COUNT"));
            updateNumericFiledValue(dataSourceBean, "maxActive_", INT, metricValueMap.get("DRUID_DATASOURCE_MAX_ACTIVE_COUNT"));
            // 汇总计算
            String databasePeer = dataSourceBean.getDatabasePeer();
            addDatabaseCount(databasePeer, groupActiveCounts, dataSourceBean.getActiveCount());
            addDatabaseCount(databasePeer, groupPoolingCounts, dataSourceBean.getPoolingCount());
            addDatabaseCount(databasePeer, groupMaxActives, dataSourceBean.getMaxActive());

            instanceActiveCount.addAndGet(dataSourceBean.getActiveCount());
            instancePoolingCount.addAndGet(dataSourceBean.getPoolingCount());
            instanceMaxCount.addAndGet(dataSourceBean.getMaxActive());
        });

        // 设置Instance对象值
        String countStatistic = appendDatabasePeer(groupActiveCounts, groupPoolingCounts, groupMaxActives);
        setFiledValue(instance, "activeCount_", instanceActiveCount.intValue());
        setFiledValue(instance, "poolingCount_", instancePoolingCount.intValue());
        setFiledValue(instance, "maxActive_", instanceMaxCount.intValue());
        setFiledValue(instance, "countStatistic_", countStatistic);
    }

    private static void addDatabaseCount(String databasePeer, Map<String, AtomicInteger> group, int count) {
        AtomicInteger atomicInteger = group.get(databasePeer);
        if (atomicInteger == null) {
            atomicInteger = new AtomicInteger();
            group.put(databasePeer, atomicInteger);
        }
        atomicInteger.addAndGet(count);
    }

    /**
     * 此方法与DruidConnectionPoolMetricProvider#appendDatabasePeer计算逻辑一致
     * @param groupActiveCounts  ActiveCount集合
     * @param groupPoolingCounts  PoolingCount集合
     * @param groupMaxActives  MaxActive集合
     * @return 拼接DatabasePeer内容
     */
    private static String appendDatabasePeer(Map<String, AtomicInteger> groupActiveCounts, Map<String, AtomicInteger> groupPoolingCounts, Map<String, AtomicInteger> groupMaxActives) {
        StringBuilder countStatistic = new StringBuilder();
        Set<String> dataBasePeerSets = groupActiveCounts.keySet();
        try {
            for (String dataBasePeer : dataBasePeerSets) {
                countStatistic.append(dataBasePeer)
                        .append("::")
                        .append(groupActiveCounts.get(dataBasePeer).intValue()).append(",")
                        .append(groupPoolingCounts.get(dataBasePeer).intValue()).append(",")
                        .append(groupMaxActives.get(dataBasePeer).intValue())
                        .append("|");
            }
        } catch (Exception e) {
            LOGGER.error("appendDatabasePeer fail.");
        }
        return countStatistic.length() > 0
                ? countStatistic.deleteCharAt(countStatistic.length() - 1).toString()
                : countStatistic.toString();
    }
}
