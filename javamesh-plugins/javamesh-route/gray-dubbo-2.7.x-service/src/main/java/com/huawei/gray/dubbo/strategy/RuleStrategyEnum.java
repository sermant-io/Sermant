/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.gray.dubbo.strategy;

import com.huawei.javamesh.core.lubanops.bootstrap.utils.StringUtils;
import com.huawei.gray.dubbo.cache.DubboCache;
import com.huawei.gray.dubbo.strategy.rule.UpstreamRuleStrategy;
import com.huawei.gray.dubbo.strategy.rule.WeightRuleStrategy;
import com.huawei.gray.dubbo.utils.RouterUtil;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.Route;

import com.alibaba.fastjson.JSONObject;

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
        if (StringUtils.isBlank(targetIp)) {
            CurrentTag currentTag = new CurrentTag();
            currentTag.setVersion(version);
            currentTag.setLdc(RouterUtil.getLdc());
            invocation.getAttachments().put(GrayConstant.GRAY_TAG, JSONObject.toJSONString(currentTag));
            if (!invocation.getAttachments().containsKey(GrayConstant.GRAY_LDC)) {
                invocation.getAttachments().put(GrayConstant.GRAY_LDC, RouterUtil.getLdc(invocation));
            }
            return DubboCache.getLocalAddr(targetService);
        }
        return targetIp;
    }
}
