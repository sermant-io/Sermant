/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.rule;

import com.huawei.flowcontrol.console.entity.DegradeRuleVo;
import com.huawei.flowcontrol.console.entity.FlowRuleVo;
import com.huawei.flowcontrol.console.entity.ParamFlowRuleVo;
import com.huawei.flowcontrol.console.entity.RuleProvider;
import com.huawei.flowcontrol.console.entity.SystemRuleVo;

import java.util.List;

/**
 * provider接口
 *
 * @param <T> 规则实体
 * @author XiaoLong Wang
 * @since 2020-12-21
 */
public interface DynamicRuleProviderExt<T> extends RuleProvider<T> {
    /**
     * 流控规则获取
     *
     * @param appName 应用名
     * @return 规则集合
     * @throws Exception zookeeper forpath异常
     */
    List<FlowRuleVo> getFlowRules(String appName) throws Exception;

    /**
     * 降级规则获取
     *
     * @param appName 应用名
     * @return 规则集合
     * @throws Exception zookeeper forpath异常
     */
    List<DegradeRuleVo> getDegradeRules(String appName) throws Exception;

    /**
     * 热点规则获取
     *
     * @param appName 应用名
     * @return 规则集合
     * @throws Exception zookeeper forpath异常
     */
    List<ParamFlowRuleVo> getParamFlowRules(String appName) throws Exception;

    /**
     * 系统规则获取
     *
     * @param appName 应用名
     * @return 规则集合
     * @throws Exception zookeeper forpath异常
     */
    List<SystemRuleVo> getSystemRules(String appName) throws Exception;
}
