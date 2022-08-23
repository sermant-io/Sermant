/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huaweicloud.loadbalancer.rule;

/**
 * 修改后负载均衡规则, 此处将添加修改前的负载均衡规则
 *
 * @author zhouss
 * @since 2022-08-15
 */
public class ChangedLoadbalancerRule extends LoadbalancerRule {
    private final LoadbalancerRule oldRule;
    private final LoadbalancerRule newRule;

    /**
     * 构造器
     *
     * @param oldRule 旧负载均衡规则
     * @param newRule 新负载均衡规则
     */
    public ChangedLoadbalancerRule(LoadbalancerRule oldRule, LoadbalancerRule newRule) {
        this.oldRule = oldRule;
        this.newRule = newRule;
        this.setRule(this.newRule.getRule());
        this.setServiceName(this.newRule.getServiceName());
    }

    public LoadbalancerRule getOldRule() {
        return oldRule;
    }

    public LoadbalancerRule getNewRule() {
        return newRule;
    }
}
