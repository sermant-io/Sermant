/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy;

import com.huawei.apm.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.strategy.rule.UpstreamRuleStrategy;
import com.huawei.gray.dubbo.strategy.rule.WeightRuleStrategy;
import com.huawei.route.common.gray.label.entity.Route;

import org.apache.dubbo.rpc.Invocation;

import java.util.List;

/**
 * 规则类型
 *
 * @author pengyuyi
 * @date 2021/10/14
 */
public enum RuleStrategyEnum {
    /**
     * 权重路由
     */
    WEIGHT(new WeightRuleStrategy()),

    /**
     * 上游路由
     */
    UPSTREAM(new UpstreamRuleStrategy()),
    ;

    private final RuleStrategy ruleStrategy;

    RuleStrategyEnum(RuleStrategy ruleStrategy) {
        this.ruleStrategy = ruleStrategy;
    }

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
    public String getTargetServiceIp(List<Route> list, String targetService, String interfaceName, String version,
            Invocation invocation) {
        String targetIp = ruleStrategy.getTargetServiceIp(list, targetService, interfaceName, version, invocation);
        return StringUtils.isBlank(targetIp) ? DubboCache.getLocalAddr(targetService) : targetIp;
    }
}
