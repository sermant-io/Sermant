/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree;

import com.huawei.hercules.service.influxdb.measurement.ibmpool.CSInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.ibmpool.JCCInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.ibmpool.JDCInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.ibmpool.MNHSInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.ibmpool.NAInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.ibmpool.NSInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.ibmpool.TLInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.ibmpool.TSInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.servermonitor.MemoryInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.servermonitor.NetworkInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.CpuInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.HeapMemoryInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.NonHeapMemoryInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.OldGCInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.ThreadInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.YoungGCInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.oraclepool.CodeCacheInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.oraclepool.MetaspaceInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.oraclepool.NewGenInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.oraclepool.OldGenInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.oraclepool.PermGenInfluxEntity;
import com.huawei.hercules.service.influxdb.measurement.skywalkingjvm.oraclepool.SurvivorInfluxEntity;

/**
 * 功能描述：Metric类型
 *
 * @author z30009938
 * @since 2021-11-23
 */
public enum MetricType {
    /**
     * 性能监控根指标
     */
    ROOT("root", false, null, JvmType.NONE, null),

    /**
     * 服务器指标
     */
    SERVER("server", false, ROOT, JvmType.NONE, null),

    /**
     * jvm中，ibm类型的jvm指标
     */
    IBM("ibm", false, ROOT, JvmType.IBM, null),

    /**
     * jvm中，open_jdk类型的jvm指标
     */
    OPEN_JDK("open_jdk", false, ROOT, JvmType.OPEN_JDK, null),

    /**
     * ibm类型中的cs指标
     */
    IBM_CS("ibm_pool_cs", true, IBM, JvmType.IBM, CSInfluxEntity.class),

    /**
     * ibm类型中的jcc指标
     */
    IBM_JCC("ibm_pool_jcc", true, IBM, JvmType.IBM, JCCInfluxEntity.class),

    /**
     * ibm类型中的JDC指标
     */
    IBM_JDC("ibm_pool_jdc", true, IBM, JvmType.IBM, JDCInfluxEntity.class),

    /**
     * ibm类型中的MNHS指标
     */
    IBM_MNHS("ibm_pool_mnhs", true, IBM, JvmType.IBM, MNHSInfluxEntity.class),

    /**
     * ibm类型中的NA指标
     */
    IBM_NA("ibm_pool_na", true, IBM, JvmType.IBM, NAInfluxEntity.class),

    /**
     * ibm类型中的NS指标
     */
    IBM_NS("ibm_pool_ns", true, IBM, JvmType.IBM, NSInfluxEntity.class),

    /**
     * ibm类型中的TL指标
     */
    IBM_TL("ibm_pool_tl", true, IBM, JvmType.IBM, TLInfluxEntity.class),

    /**
     * ibm类型中的TS指标
     */
    IBM_TS("ibm_pool_ts", true, IBM, JvmType.IBM, TSInfluxEntity.class),

    /**
     * OPEN_JDK类型中的CPU指标
     */
    OPENJDK_CPU("oracle_jvm_monitor_cpu", true, OPEN_JDK, JvmType.OPEN_JDK, CpuInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标HEAP_MEMORY
     */
    OPENJDK_HEAP_MEMORY("oracle_jvm_monitor_heap_memory", true, OPEN_JDK, JvmType.OPEN_JDK, HeapMemoryInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标NON_HEAP_MEMORY
     */
    OPENJDK_NON_HEAP_MEMORY("oracle_jvm_monitor_non_heap_memory", true, OPEN_JDK, JvmType.OPEN_JDK, NonHeapMemoryInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标OLD_GC
     */
    OPENJDK_OLD_GC("oracle_jvm_monitor_old_gc", true, OPEN_JDK, JvmType.OPEN_JDK, OldGCInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标oracle_jvm_monitor_thread
     */
    OPENJDK_THREAD("oracle_jvm_monitor_thread", true, OPEN_JDK, JvmType.OPEN_JDK, ThreadInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标oracle_jvm_monitor_young_gc
     */
    OPENJDK_YOUNG_GC("oracle_jvm_monitor_young_gc", true, OPEN_JDK, JvmType.OPEN_JDK, YoungGCInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标pool
     */
    OPENJDK_POOL("pool", false, OPEN_JDK, JvmType.OPEN_JDK, null),

    /**
     * OPEN_JDK类型中的指标oracle_pool_code_cache
     */
    OPENJDK_CODE_CACHE("oracle_pool_code_cache", true, OPENJDK_POOL, JvmType.OPEN_JDK, CodeCacheInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标oracle_pool_metaspace
     */
    OPENJDK_METASPACE("oracle_pool_metaspace", true, OPENJDK_POOL, JvmType.OPEN_JDK, MetaspaceInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标oracle_pool_new_gen
     */
    OPENJDK_NEW_GEN("oracle_pool_new_gen", true, OPENJDK_POOL, JvmType.OPEN_JDK, NewGenInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标oracle_pool_old_gen
     */
    OPENJDK_OLD_GEN("oracle_pool_old_gen", true, OPENJDK_POOL, JvmType.OPEN_JDK, OldGenInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标oracle_pool_perm_gen
     */
    OPENJDK_PERM_GEN("oracle_pool_perm_gen", true, OPENJDK_POOL, JvmType.OPEN_JDK, PermGenInfluxEntity.class),

    /**
     * OPEN_JDK类型中的指标oracle_pool_survivor_space
     */
    OPENJDK_SURVIVOR("oracle_pool_survivor_space", true, OPENJDK_POOL, JvmType.OPEN_JDK, SurvivorInfluxEntity.class),

    /**
     * SERVER指标CPU
     */
    SERVER_CPU("server_monitor_cpu", true, OPENJDK_POOL, JvmType.OPEN_JDK, com.huawei.hercules.service.influxdb.measurement.servermonitor.CpuInfluxEntity.class),

    /**
     * SERVER指标MEMORY
     */
    SERVER_MEMORY("server_monitor_memory", true, OPENJDK_POOL, JvmType.OPEN_JDK, MemoryInfluxEntity.class),

    /**
     * SERVER指标NETWORK
     */
    SERVER_NETWORK("server_monitor_network", true, OPENJDK_POOL, JvmType.OPEN_JDK, NetworkInfluxEntity.class);

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 父指标类型
     */
    private final MetricType parentMetricType;

    /**
     * jvm类型
     */
    private final JvmType jvmType;

    /**
     * 指标是否是数据节点，即这个指标是一系列子指标的统称，还是一个数据指标，需要去influxdb查询数据
     */
    private final boolean isDataNode;

    /**
     * influxdb中封装的数据对应javabean类型
     */
    private final Class<?> dataClassType;

    /**
     * 枚举初始化
     *
     * @param name             枚举名称， 如果是数据节点，则相当于是influxdb的measurement名称
     * @param isDataNode       是否是数据节点，是数据几点则需要在influxdb中有对应measurement
     * @param parentMetricType 父节点类型
     * @param jvmType          如果是jvm指标，则需要指定jvm类型
     * @param dataClassType    数据javaBean类型
     */
    MetricType(String name, boolean isDataNode, MetricType parentMetricType, JvmType jvmType, Class<?> dataClassType) {
        this.name = name;
        this.isDataNode = isDataNode;
        this.parentMetricType = parentMetricType;
        this.jvmType = jvmType;
        this.dataClassType = dataClassType;
    }

    public String getName() {
        return name;
    }

    public boolean isDataNode() {
        return isDataNode;
    }

    public MetricType getParentMetricType() {
        return parentMetricType;
    }

    public JvmType getJvmType() {
        return jvmType;
    }

    public Class<?> getDataClassType() {
        return dataClassType;
    }
}
