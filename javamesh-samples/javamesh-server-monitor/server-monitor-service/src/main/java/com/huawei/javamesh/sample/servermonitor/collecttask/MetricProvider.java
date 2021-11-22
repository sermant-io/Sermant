/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.javamesh.sample.servermonitor.collecttask;

import java.util.List;

/**
 * Metric provider
 *
 * @param <M> 指标类型
 */
public interface MetricProvider<M> {

    /**
     * 采集
     *
     * @return 采集的指标
     */
    M collect();

    /**
     * 消费已采集的指标列表
     *
     * @param metrics 已采集的指标列表
     */
    void consume(List<M> metrics);
}
