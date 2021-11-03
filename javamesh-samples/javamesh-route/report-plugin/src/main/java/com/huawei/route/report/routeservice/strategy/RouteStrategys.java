/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.routeservice.strategy;

/**
 * 路由策略枚举类
 *
 * @author wl
 * @since 2021-06-16
 */
public enum RouteStrategys {
    /**
     * 随机策略
     */
    RANDOM_STRATEGY(RandomStrategyImpl.getInstance()),

    /**
     * 轮询策略
     */
    ROUND_STRATEGY(RoundStrategyImpl.getInstance()),

    /**
     * 轮询策略
     */
    CONSISTENT_HASH_STRATEGY(ConsistentHashStrategyImpl.getInstance());

    private InterfaceRouteStrategy strategy;

    public InterfaceRouteStrategy getStrategy() {
        return strategy;
    }

    RouteStrategys(InterfaceRouteStrategy strategy) {
        this.strategy = strategy;
    }
}
