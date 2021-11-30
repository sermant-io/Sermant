/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.gray.feign.rule;

import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.label.entity.Route;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 路由策略
 *
 * @author lilai
 * @since 2021-11-03
 */
public interface RuleStrategy {
    /**
     * 获取目标地址ip
     *
     * @param list          路由规则
     * @param targetService 目标服务
     * @param headers       http请求头
     * @return 可路由的应用实例
     */
    Instances getTargetServiceInstance(List<Route> list, String targetService, Map<String, Collection<String>> headers);
}
