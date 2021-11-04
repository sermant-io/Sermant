/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.common.report.routeservice;

import com.huawei.route.common.report.routeservice.strategy.InterfaceRouteStrategy;

import java.util.List;

/**
 * 本地路由
 *
 * @author wl
 * @since 2021-06-08
 */
public class RouteService {
    private RouteService() {
    }

    public static <T> T getAddrByRoute(InterfaceRouteStrategy strategy, List<T> addrList, String param) {
        return strategy.route(addrList, param);
    }
}
