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

import com.huaweicloud.loadbalancer.service.RuleConverter;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 负载均衡规则解析器
 *
 * @author zhouss
 * @since 2022-08-09
 */
public class LoadbalancerRuleResolver implements RuleResolver<LoadbalancerRule> {
    private static final String LOAD_BALANCER_PREFIX = "servicecomb.loadbalancer";

    private final RuleConverter converter;

    /**
     * 规则缓存
     */
    private final Map<String, LoadbalancerRule> rules = new HashMap<>();

    /**
     * 规则构造器
     */
    public LoadbalancerRuleResolver() {
        this.converter = PluginServiceManager.getPluginService(RuleConverter.class);
    }

    @Override
    public Optional<LoadbalancerRule> resolve(DynamicConfigEvent event) {
        final String key = event.getKey();
        if (!key.startsWith(LOAD_BALANCER_PREFIX)) {
            return Optional.empty();
        }
        String businessKey = key.substring(LOAD_BALANCER_PREFIX.length() + 1);
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            rules.remove(businessKey);
        }
        final Optional<LoadbalancerRule> rule = converter.convert(event.getContent());
        if (rule.isPresent()) {
            rules.put(businessKey, rule.get());
            return rule;
        }
        return Optional.empty();
    }

    /**
     * 获取目标服务的负载均衡类型
     *
     * @param serviceName 目标服务名
     * @return LoadbalancerRule
     */
    public Optional<LoadbalancerRule> getTargetServiceRule(String serviceName) {
        return rules.values().stream().filter(rule -> StringUtils.equals(serviceName, rule.getServiceName()))
                .findAny();
    }
}
