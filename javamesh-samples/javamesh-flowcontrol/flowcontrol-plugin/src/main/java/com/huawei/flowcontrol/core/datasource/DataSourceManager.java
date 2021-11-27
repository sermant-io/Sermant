/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.core.datasource;

/**
 * 数据源管理器接口类
 *
 * @author liyi
 * @since 2020-08-26
 */
public interface DataSourceManager {
    /**
     * 初始化规则
     */
    void start();

    /**
     * 停止方法
     */
    void stop();
}
