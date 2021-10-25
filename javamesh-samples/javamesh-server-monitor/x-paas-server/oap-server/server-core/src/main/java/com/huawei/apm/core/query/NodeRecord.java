/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.query;

import com.huawei.apm.core.define.ConnectionPoolConstants;
import com.huawei.apm.core.source.NodeRecordType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.apache.skywalking.apm.util.StringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 定义节点信息
 *
 * @author zhouss
 * @since 2020-11-29
 */
@Getter
@Setter
@Builder
public class NodeRecord {
    /**
     * 当前节点名称
     */
    private String name;

    /**
     * 父节点名称
     */
    private String parentName;

    /**
     * 活动连接数
     */
    private long activeCount;

    /**
     * 可用连接数
     */
    private long poolingCount;

    /**
     * 最大连接数
     */
    private long maxActive;

    /**
     * 节点编码
     */
    private String entityId;

    /**
     * 父节点编码
     */
    private String serviceId;

    /**
     * 节点类型
     */
    private NodeRecordType type;

    /**
     * 数据库实例
     */
    private String databasePeers;

    /**
     * 各连接数指标叠加
     *
     * @param record 节点记录
     */
    public void add(NodeRecord record) {
        if (record == null) {
            return;
        }
        this.activeCount += record.getActiveCount();
        this.poolingCount += record.getPoolingCount();
        this.maxActive += record.getMaxActive();
    }

    /**
     * 将字符串转换为map
     *
     * @param dataBasePeers 数据库实例指标  localhost:3306::0,20,50|localhost:3307::0,10,30
     * @return 数据库实例，以及对应的连接数键值集合
     */
    public static Map<String, ConnectionsCountMetric> convertDatabasePeers2Map(String dataBasePeers) {
        if (StringUtil.isEmpty(dataBasePeers)) {
            return Collections.EMPTY_MAP;
        }
        HashMap<String, ConnectionsCountMetric> metricMap =
                new HashMap<>(ConnectionPoolConstants.DEFAULT_MAP_INIT_CAPACITY);
        String[] databasePeerMetrics = dataBasePeers
                .split(ConnectionPoolConstants.SEPARATOR_DATABASE_PEER);
        for (String databasePeerMetric : databasePeerMetrics) {
            // 将数据库实例与连接数数据切分 ，第一个下标表示数据库实例,例如localhost:3306,
            // 第二个下标表示三个连接数(活动连接数、可用连接数、最大连接数),例如0,20,50
            String[] peerAndConnectionCounts = databasePeerMetric.split("::");
            if (peerAndConnectionCounts.length != 2) {
                continue;
            }
            String[] connectionCounts = peerAndConnectionCounts[1].split(",");
            if (connectionCounts.length != ConnectionPoolConstants.CONNECTION_COUNT_LENGTH) {
                continue;
            }
            metricMap.put(peerAndConnectionCounts[0],
                    ConnectionsCountMetric.builder()
                            .activeCount(
                                    Integer.parseInt(connectionCounts[ConnectionPoolConstants.ACTIVE_COUNT_INDEX]))
                            .poolingCount(
                                    Integer.parseInt(connectionCounts[ConnectionPoolConstants.POOLING_COUNT_INDEX]))
                            .maxCount(
                                    Integer.parseInt(connectionCounts[ConnectionPoolConstants.MAX_ACTIVE_INDEX]))
                            .build());
        }
        return metricMap;
    }
}
