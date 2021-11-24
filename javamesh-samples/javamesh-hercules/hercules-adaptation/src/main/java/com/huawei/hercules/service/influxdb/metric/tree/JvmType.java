/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.hercules.service.influxdb.metric.tree;

/**
 * 功能描述：jvm类型
 *
 * @author z30009938
 * @since 2021-11-23
 */
public enum JvmType {
    /**
     * IBM版本jvm
     */
    IBM("ibm"),

    /**
     * open_jdk版本类型jvm
     */
    OPEN_JDK("open_jdk");

    /**
     * jvm类型标识符
     */
    private final String name;

    JvmType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
