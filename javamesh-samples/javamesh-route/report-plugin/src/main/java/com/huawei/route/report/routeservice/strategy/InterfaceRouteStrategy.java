/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.report.routeservice.strategy;

import java.util.List;

/**
 * 路由策略接口
 *
 * @author wl
 * @since 2021-06-08
 */
public interface InterfaceRouteStrategy {
    /**
     * 具体路由策略
     *
     * @param addrList 来自server的有效待选集合
     * @param param    需要传参时使用，如一致性hash算法
     * @param <T> 表示addrList元素类型
     * @return 返回根据路由策略计算后得到的元素
     */
    <T> T route(List<T> addrList, String param);
}
