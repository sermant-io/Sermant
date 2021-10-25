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
 * ibm jvm内存的实体类
 *
 * @author zhengbin zhao
 * @since 2021-03-16
 */
@ScopeDeclaration(id = ExtScopeDefine.SERVICE_INSTANCE_IBM_JVM_MEMORY_POOL, name = "ServiceInstanceIBMJVMMemoryPool",
        catalog = DefaultScopeDefine.SERVICE_INSTANCE_CATALOG_NAME)
@ScopeDefaultColumn.VirtualColumnDefinition(fieldName = "entityId", columnName = "entity_id", isID = true,
        type = String.class)
public class ServiceInstanceIBMJVMMemoryPool extends Source {
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
    private IBMMemoryPoolType poolType;
    @Getter
    @Setter
    private long init;
    @Getter
    @Setter
    private long max;
    @Getter
    @Setter
    private long used;
    @Getter
    @Setter
    private long committed;

    @Override
    public int scope() {
        return ExtScopeDefine.SERVICE_INSTANCE_IBM_JVM_MEMORY_POOL;
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