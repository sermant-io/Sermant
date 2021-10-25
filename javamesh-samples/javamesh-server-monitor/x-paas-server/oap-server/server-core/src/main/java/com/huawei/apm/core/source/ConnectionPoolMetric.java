/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.apm.core.source;

import com.huawei.apm.core.define.ExtScopeDefine;

import lombok.Getter;
import lombok.Setter;

import org.apache.skywalking.oap.server.core.source.ScopeDeclaration;
import org.apache.skywalking.oap.server.core.source.ScopeDefaultColumn;
import org.apache.skywalking.oap.server.core.source.Source;

/**
 * 定义存储应用，数据源，实例的数据结构
 *
 * @author zhouss
 * @since 2020-11-29
 */
@ScopeDeclaration(
        id = ExtScopeDefine.CONNECTION_POOL_METRIC,
        name = "ConnectionPoolMetric",
        catalog = ExtScopeDefine.SERVICE_INSTANCE_CATALOG_NAME)
@ScopeDefaultColumn.VirtualColumnDefinition(
        fieldName = "entityId",
        columnName = "entity_id",
        isID = true,
        type = String.class)
public class ConnectionPoolMetric extends Source {
    /**
     * 活动连接数
     */
    public static final String ACTIVE_COUNT = "active_count";

    /**
     * 空闲连接数
     */
    public static final String POOLING_COUNT = "pooling_count";

    /**
     * 最大连接数
     */
    public static final String MAX_ACTIVE = "max_active";

    /**
     * 父节点id
     */
    public static final String SERVICE_ID = "service_id";

    /**
     * 当前节点id
     */
    public static final String ENTITY_ID = "entity_id";

    /**
     * 当前节点名称
     */
    public static final String NAME = "name";

    /**
     * 父节点名称
     */
    public static final String PARENT_NAME = "parent_name";

    /**
     * 数据库实例
     * 如果是ip实例，则以集合方式存储
     * 如果是数据源，则只是存储单个实例
     */
    public static final String DATABASE_PEERS = "database_peers";
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = NAME)
    private String name;

    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = SERVICE_ID)
    private String serviceId;

    /**
     * 父维度名称
     */
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = PARENT_NAME)
    private String parentName;

    /**
     * 活动连接数
     */
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = ACTIVE_COUNT)
    private long activeCount;

    /**
     * 空闲连接数
     */
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = POOLING_COUNT)
    private long poolingCount;

    /**
     * 最大连接数
     */
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = MAX_ACTIVE)
    private long maxActive;

    /**
     * 数据库实例
     * 如果是实例，有多个数据源，以及对应的连接数，按照顺序是活动连接数，可用连接数，最大连接数：localhost:3306::1,2,3|localhost:3306::1,2,3
     * 如果是数据源: localhost:3306
     */
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = DATABASE_PEERS)
    private String dataBasePeers;

    /**
     * 活动连接数与最大连接数的比例
     */
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "ratio", requireDynamicActive = true)
    private double ratio;

    /**
     * 维度类型
     * APP 应用
     * IP_INSTANCE 实例
     * DATASOURCE 数据源
     */
    @Getter
    @Setter
    private NodeRecordType dimensionType;

    /**
     * 数据连接池类型
     */
    @Getter
    @Setter
    private ConnectionPoolType connectionPoolType;

    @Override
    public int scope() {
        return ExtScopeDefine.CONNECTION_POOL_METRIC;
    }

    @Override
    public String getEntityId() {
        return String.valueOf(id);
    }

    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "copy", requireDynamicActive = false)
    private int copy;// huawei update.无损演练新增属性
}
