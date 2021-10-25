/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.source;

import com.huawei.apm.core.define.ExtScopeDefine;

import lombok.Getter;
import lombok.Setter;

import org.apache.skywalking.oap.server.core.source.DefaultScopeDefine;
import org.apache.skywalking.oap.server.core.source.ScopeDeclaration;
import org.apache.skywalking.oap.server.core.source.ScopeDefaultColumn;
import org.apache.skywalking.oap.server.core.source.Source;

/**
 * 服务器磁盘IO的实体类
 *
 * @author zhengbin zhao
 * @since 2021-02-25
 */
@ScopeDeclaration(
        id = ExtScopeDefine.SERVER_MONITOR_DISK_METRIC,
        name = "ServerMonitorDiskMetric",
        catalog = DefaultScopeDefine.SERVICE_INSTANCE_CATALOG_NAME)
@ScopeDefaultColumn.VirtualColumnDefinition(
        fieldName = "entityId",
        columnName = "entity_id",
        isID = true,
        type = String.class)
public class ServerMonitorDiskMetric extends Source {
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "name", requireDynamicActive = true)
    private String name;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "service_name", requireDynamicActive = true)
    private String serviceName;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "service_id")
    private String serviceId;
    @Getter
    @Setter
    private long ioRead;
    @Getter
    @Setter
    private long ioWrite;
    @Getter
    @Setter
    private double ioBusy;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "disk_name")
    private String diskName;
    @Getter
    @Setter
    @ScopeDefaultColumn.DefinedByField(columnName = "query_id")
    private String queryId;

    @Override
    public int scope() {
        return ExtScopeDefine.SERVER_MONITOR_DISK_METRIC;
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
