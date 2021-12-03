/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
