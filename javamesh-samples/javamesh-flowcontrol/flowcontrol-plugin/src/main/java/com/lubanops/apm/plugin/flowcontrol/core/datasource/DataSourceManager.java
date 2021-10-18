/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.apm.plugin.flowcontrol.core.datasource;

/**
 * 数据源管理器接口类
 *
 * @author liyi
 * @since 2020-08-26
 */
@FunctionalInterface
public interface DataSourceManager {
    /**
     * 初始化规则
     */
    void initRules();
}
