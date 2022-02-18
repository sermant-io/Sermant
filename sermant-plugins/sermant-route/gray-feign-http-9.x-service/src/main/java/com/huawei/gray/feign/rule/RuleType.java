/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.gray.feign.rule;

import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.label.entity.Route;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 路由类型
 *
 * @author lilai
 * @since 2021-11-03
 */
public enum RuleType {
    /**
     * 权重路由
     */
    WEIGHT(new WeightRuleStrategy()),

    /**
     * 上游路由
     */
    UPSTREAM(new UpstreamRuleStrategy());

    private final RuleStrategy ruleStrategy;

    RuleType(RuleStrategy ruleStrategy) {
        this.ruleStrategy = ruleStrategy;
    }

    /**
     * 获取目标地址ip
     *
     * @param list 路由规则
     * @param targetService 目标服务
     * @param headers http请求头
     * @return 可路由的应用实例
     */
    public Instances getTargetServiceInstance(List<Route> list, String targetService,
        Map<String, Collection<String>> headers) {
        return ruleStrategy.getTargetServiceInstance(list, targetService, headers);
    }
}