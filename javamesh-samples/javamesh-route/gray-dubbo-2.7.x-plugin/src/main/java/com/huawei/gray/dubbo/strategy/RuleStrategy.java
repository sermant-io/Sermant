/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy;

import com.huawei.route.common.gray.label.entity.Route;

import org.apache.dubbo.rpc.Invocation;

import java.util.List;

/**
 * 路由策略
 *
 * @author pengyuyi
 * @date 2021/10/14
 */
public interface RuleStrategy {
    /**
     * 获取目标地址ip
     *
     * @param list 路由规则
     * @param targetService 目标服务
     * @param interfaceName 接口
     * @param version 当前服务的版本
     * @param invocation dubbo invocation
     * @return 目标地址 ip:port
     */
    String getTargetServiceIp(List<Route> list, String targetService, String interfaceName, String version,
            Invocation invocation);
}
