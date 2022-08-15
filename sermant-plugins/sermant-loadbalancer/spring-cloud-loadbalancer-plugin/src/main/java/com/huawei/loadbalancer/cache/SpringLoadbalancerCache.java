/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.loadbalancer.cache;

import com.huaweicloud.loadbalancer.config.LbContext;
import com.huaweicloud.loadbalancer.config.SpringLoadbalancerType;
import com.huaweicloud.loadbalancer.rule.LoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * spring loadbalancer负载均衡器缓存
 *
 * @author provenceee
 * @since 2022-01-20
 */
public enum SpringLoadbalancerCache {
    /**
     * 单例
     */
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 参数缓存
     *
     * @see org.springframework.beans.factory.ObjectProvider
     */
    private final Map<String, Object> providerMap = new ConcurrentHashMap<>();

    /**
     * 原来的宿主的负载均衡器的缓存
     */
    private final Map<String, Object> originCache = new ConcurrentHashMap<>();

    /**
     * 新的负载均衡器的缓存
     */
    private final Map<String, Optional<Object>> newCache = new ConcurrentHashMap<>();

    SpringLoadbalancerCache() {
        RuleManager.INSTANCE.addRuleListener(this::updateCache);
    }

    private void updateCache(LoadbalancerRule rule, DynamicConfigEvent event) {
        if (rule.getRule() == null || !LbContext.INSTANCE.isTargetLb(LbContext.LOADBALANCER_SPRING)) {
            return;
        }
        final Optional<SpringLoadbalancerType> springLoadbalancerType = SpringLoadbalancerType
                .matchLoadbalancer(rule.getRule());
        if (!springLoadbalancerType.isPresent()) {
            LOGGER.fine(String.format(Locale.ENGLISH, "Can not support spring loadbalancer rule: [%s]",
                    rule.getRule()));
            return;
        }
        final String serviceName = rule.getServiceName();
        if (serviceName == null) {
            newCache.clear();
            return;
        }
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            newCache.put(serviceName, Optional.ofNullable(originCache.get(serviceName)));
        } else {
            newCache.remove(serviceName);
        }
    }

    /**
     * 存入provider
     *
     * @param serviceId 服务id
     * @param provider provider
     */
    public void putProvider(String serviceId, Object provider) {
        providerMap.putIfAbsent(serviceId, provider);
    }

    /**
     * 获取provider
     *
     * @param serviceId 服务id
     * @return provider
     */
    public Object getProvider(String serviceId) {
        return providerMap.get(serviceId);
    }

    /**
     * 存入原来的负载均衡器
     *
     * @param serviceId 服务id
     * @param loadBalancer 负载均衡器
     */
    public void putOrigin(String serviceId, Object loadBalancer) {
        originCache.putIfAbsent(serviceId, loadBalancer);
    }

    /**
     * 获取原来的负载均衡器
     *
     * @param serviceId 服务id
     * @return 负载均衡器
     */
    public Object getOrigin(String serviceId) {
        return originCache.get(serviceId);
    }

    public Map<String, Optional<Object>> getNewCache() {
        return newCache;
    }
}
