/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.routeservice.strategy;

import com.huawei.route.common.report.common.utils.HashcodeUtil;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性hash算法
 *
 * @author wl
 * @since 2021-06-08
 */
public class ConsistentHashStrategyImpl implements InterfaceRouteStrategy {
    private static final int VIRTUAL_NODE_NUM = 100;

    private ConsistentHashStrategyImpl() {
    }

    /**
     * 对外提供的获取实例的方法
     *
     * @return 返回实例对象
     */
    public static ConsistentHashStrategyImpl getInstance() {
        return ConsistentHashStrategyImplHolder.INSTANCE;
    }

    private static class ConsistentHashStrategyImplHolder {
        private static final ConsistentHashStrategyImpl INSTANCE = new ConsistentHashStrategyImpl();
    }

    @Override
    public <T> T route(List<T> addrList, String param) {
        TreeMap<Long, T> addressRing = new TreeMap<Long, T>();
        for (T address : addrList) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                long addressHash = HashcodeUtil.fnv132Hash("SHARD-" + address + "-NODE-" + i);
                addressRing.put(addressHash, address);
            }
        }

        Long requestAddrHashcode = HashcodeUtil.fnv132Hash(param);
        SortedMap<Long, T> tailMap = addressRing.tailMap(requestAddrHashcode);
        if (!tailMap.isEmpty()) {
            return tailMap.get(tailMap.firstKey());
        }
        return addressRing.firstEntry().getValue();
    }
}
