/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.routeservice.strategy;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询策略
 *
 * @author wl
 * @since 2021-06-08
 */
public class RoundStrategyImpl implements InterfaceRouteStrategy {
    private AtomicInteger nextAddrCounter = new AtomicInteger();

    private RoundStrategyImpl() {
    }

    /**
     * 对外提供的获取实例的方法
     *
     * @return 返回实例对象
     */
    public static RoundStrategyImpl getInstance() {
        return RoundStrategyImplHolder.INSTANCE;
    }

    private static class RoundStrategyImplHolder {
        private static final RoundStrategyImpl INSTANCE = new RoundStrategyImpl();
    }

    private int incrementAndGetIndex(int count) {
        int current;
        int next;
        do {
            current = nextAddrCounter.get();
            next = (current + 1) % count;
        } while (!nextAddrCounter.compareAndSet(current, next));
        return next;
    }

    @Override
    public <T> T route(List<T> addrList, String param) {
        int index = incrementAndGetIndex(addrList.size());
        return addrList.get(index);
    }
}
