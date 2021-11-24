/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree;

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
    ROOT("root", false),

    /**
     * 服务器指标
     */
    SERVER("server", false),

    /**
     * jvm中，ibm类型的jvm指标
     */
    IBM("ibm", false),

    /**
     * jvm中，open_jdk类型的jvm指标
     */
    OPEN_JDK("open_jdk", false),

    /**
     * ibm类型中的cs指标
     */
    IBM_CS("ibm_pool_cs", true);

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 指标是否是数据节点，即这个指标是一系列子指标的统称，还是一个数据指标，需要去influxdb查询数据
     */
    private final boolean isDataNode;

    MetricType(String name, boolean isDataNode) {
        this.name = name;
        this.isDataNode = isDataNode;
    }

    public String getName() {
        return name;
    }

    public boolean isDataNode() {
        return isDataNode;
    }
}
