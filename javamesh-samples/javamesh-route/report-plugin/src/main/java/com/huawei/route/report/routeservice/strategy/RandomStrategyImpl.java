/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.routeservice.strategy;

import java.util.List;
import java.util.Random;

/**
 * 随机策略
 *
 * @author wl
 * @since 2021-06-08
 */
public class RandomStrategyImpl implements InterfaceRouteStrategy {
    private Random rand;

    private RandomStrategyImpl() {
        rand = new Random(System.currentTimeMillis());
    }

    /**
     * 对外提供的获取实例的方法
     *
     * @return 返回实例对象
     */
    public static RandomStrategyImpl getInstance() {
        return RandomStrategyImplHolder.INSTANCE;
    }

    private static class RandomStrategyImplHolder {
        private static final RandomStrategyImpl INSTANCE = new RandomStrategyImpl();
    }

    @Override
    public <T> T route(List<T> addrList, String param) {
        return addrList.get(rand.nextInt(addrList.size()));
    }
}
