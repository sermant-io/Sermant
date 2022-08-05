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

import com.huawei.loadbalancer.cache.LoadbalancerCache;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.config.SpringLoadbalancerType;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * LoadBalancerClientFactory增强类
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
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        getLoadBalancer((String) context.getArguments()[0]).ifPresent(context::changeResult);
        return context;
    }

    private Optional<Object> getLoadBalancer(String serviceId) {
        if (config == null || config.getSpringType() == null) {
            // 没有配置的情况下return null，不影响原方法
            return Optional.empty();
        }
        Class<?> clazz = getLoadBalancerClass(config.getSpringType());

        // 如果原来的负载均衡器跟需要的一样，就不需要new了，直接return null，不影响原方法
        if (LoadbalancerCache.INSTANCE.getOrigin(serviceId).getClass() == clazz) {
            return Optional.empty();
        }
        return LoadbalancerCache.INSTANCE.getNewCache().computeIfAbsent(serviceId, value -> {
            try {
                Constructor<?> constructor = clazz.getConstructor(ObjectProvider.class, String.class);
                return Optional.of(constructor.newInstance(LoadbalancerCache.INSTANCE.getProvider(serviceId),
                    serviceId));
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
                LOGGER.warning("Cannot get the loadbalancer.");
                return Optional.empty();
            }
        });
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
