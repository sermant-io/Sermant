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

package com.huawei.loadbalancer.interceptor;

import com.huawei.loadbalancer.cache.SpringLoadbalancerCache;

import com.huaweicloud.loadbalancer.config.LbContext;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.config.SpringLoadbalancerType;
import com.huaweicloud.loadbalancer.rule.LoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * LoadBalancerClientFactory增强类-针对spring loadbalancer
 *
 * @author provenceee
 * @since 2022-01-20
 */
public class ClientFactoryInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final LoadbalancerConfig config;

    /**
     * 构造方法
     */
    public ClientFactoryInterceptor() {
        config = PluginConfigManager.getPluginConfig(LoadbalancerConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        LbContext.INSTANCE.setCurLoadbalancerType(LbContext.LOADBALANCER_SPRING);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        getLoadBalancer((String) context.getArguments()[0]).ifPresent(context::changeResult);
        return context;
    }

    private Optional<Object> getLoadBalancer(String serviceId) {
        if (config == null || !RuleManager.INSTANCE.isConfigured()) {
            // 没有配置的情况下return null, 不影响原方法
            return Optional.empty();
        }
        final Optional<Object> cacheBalancer = SpringLoadbalancerCache.INSTANCE.getNewCache().get(serviceId);
        if (cacheBalancer != null) {
            return cacheBalancer;
        }
        final Optional<SpringLoadbalancerType> springLoadbalancerType = matchLoadbalancerType(serviceId);
        if (!springLoadbalancerType.isPresent()) {
            return useDefaultRule(serviceId);
        }
        Class<?> clazz = getLoadBalancerClass(springLoadbalancerType.get());

        // 如果原来的负载均衡器跟需要的一样，就不需要new了，直接return null，不影响原方法
        if (SpringLoadbalancerCache.INSTANCE.getOrigin(serviceId).getClass() == clazz) {
            return Optional.empty();
        }
        return SpringLoadbalancerCache.INSTANCE.getNewCache().computeIfAbsent(serviceId,
            value -> createLoadbalancer(springLoadbalancerType.get(), serviceId));
    }

    private Optional<Object> useDefaultRule(String serviceId) {
        final String defaultRule = config.getDefaultRule();
        if (defaultRule == null) {
            return Optional.empty();
        }
        final Optional<SpringLoadbalancerType> springLoadbalancerType = SpringLoadbalancerType
                .matchLoadbalancer(defaultRule);
        if (!springLoadbalancerType.isPresent()) {
            return Optional.empty();
        }
        return SpringLoadbalancerCache.INSTANCE.getNewCache().computeIfAbsent(serviceId,
            value -> createLoadbalancer(springLoadbalancerType.get(), serviceId));
    }

    private Optional<Object> createLoadbalancer(SpringLoadbalancerType type, String serviceId) {
        Class<?> clazz = getLoadBalancerClass(type);
        try {
            Constructor<?> constructor = clazz.getConstructor(ObjectProvider.class, String.class);
            return Optional.of(constructor.newInstance(SpringLoadbalancerCache.INSTANCE.getProvider(serviceId),
                    serviceId));
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Cannot create loadbalancer [%s].", clazz.getName()));
            return Optional.empty();
        }
    }

    private Optional<SpringLoadbalancerType> matchLoadbalancerType(String serviceId) {
        final Optional<LoadbalancerRule> targetServiceRule = RuleManager.INSTANCE.getTargetServiceRule(serviceId);
        if (!targetServiceRule.isPresent()) {
            return Optional.empty();
        }
        return SpringLoadbalancerType
                .matchLoadbalancer(targetServiceRule.get().getRule());
    }

    private Class<?> getLoadBalancerClass(SpringLoadbalancerType type) {
        try {
            return type == SpringLoadbalancerType.RANDOM ? RandomLoadBalancer.class : RoundRobinLoadBalancer.class;
        } catch (NoClassDefFoundError e) {
            // 低版本没有RandomLoadBalancer，返回RoundRobinLoadBalancer
            return RoundRobinLoadBalancer.class;
        }
    }
}
