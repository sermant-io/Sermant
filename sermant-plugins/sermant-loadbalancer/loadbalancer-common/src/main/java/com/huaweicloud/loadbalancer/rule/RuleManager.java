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

import com.huaweicloud.loadbalancer.listener.CacheListener;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;

import java.util.Optional;

/**
 * 规则manager
 *
 * @author zhouss
 * @since 2022-08-10
 */
public enum RuleManager {
    /**
     * 单例
     */
    INSTANCE;

    private final LoadbalancerRuleResolver loadbalancerRuleRuleResolver = new LoadbalancerRuleResolver();

    /**
     * 解析配置
     *
     * @param event 事件
     */
    public void resolve(DynamicConfigEvent event) {
        loadbalancerRuleRuleResolver.resolve(event);
    }

    /**
     * 获取目标服务的负载均衡类型
     *
     * @param serviceName 目标服务名
     * @return LoadbalancerRule
     */
    public Optional<LoadbalancerRule> getTargetServiceRule(String serviceName) {
        return loadbalancerRuleRuleResolver.getTargetServiceRule(serviceName);
    }

    /**
     * 添加缓存监听器
     *
     * @param cacheListener 监听器
     */
    public void addRuleListener(CacheListener cacheListener) {
        loadbalancerRuleRuleResolver.addListener(cacheListener);
    }

    /**
     * 宿主服务是否配置负载均衡策略
     *
     * @return true为已配置
     */
    public boolean isConfigured() {
        return loadbalancerRuleRuleResolver.isConfigured();
    }
}
