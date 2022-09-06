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

package com.huaweicloud.loadbalancer.interceptor;

import com.huaweicloud.loadbalancer.cache.RibbonLoadbalancerCache;
import com.huaweicloud.loadbalancer.config.LbContext;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.config.RibbonLoadbalancerType;
import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.IRule;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Ribbon BaseLoadBalancer负载均衡增强类
 *
 * @author provenceee
 * @since 2022-02-24
 */
public class RibbonLoadBalancerInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 默认的ribbon负载均衡键
     */
    private static final String DEFAULT_RIBBON_LOADBALANCER_KEY = "default";

    /**
     * 存储哪些服务备份过负载均衡, 无需考虑线程安全
     */
    private final Set<String> backUpMarks = new HashSet<>();

    /**
     * 服务负载均衡缓存key: 服务名, value: 负载均衡缓存
     */
    private final Map<String, Map<RibbonLoadbalancerType, AbstractLoadBalancerRule>> servicesRuleMap =
            new ConcurrentHashMap<>();

    private final Function<RibbonLoadbalancerType, Optional<AbstractLoadBalancerRule>> ruleCreator = type -> {
        final String clazzName = type.getClazzName();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final Class<?> ruleClazz = contextClassLoader.loadClass(clazzName);
            return Optional.of((AbstractLoadBalancerRule) ruleClazz.newInstance());
        } catch (ClassNotFoundException exception) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Can not load rule clazz [%s]", clazzName));
        } catch (InstantiationException | IllegalAccessException ex) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Can not create rule clazz [%s] by no args constructor",
                    clazzName));
        }
        return Optional.empty();
    };

    private final LoadbalancerConfig config;

    /**
     * 构造方法
     */
    public RibbonLoadBalancerInterceptor() {
        this.config = PluginConfigManager.getPluginConfig(LoadbalancerConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        LbContext.INSTANCE.setCurLoadbalancerType(LbContext.LOADBALANCER_RIBBON);
        setRule(context);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private AbstractLoadBalancerRule getTargetRule(String serviceName, RibbonLoadbalancerType type) {
        return getTargetServiceRuleMap(serviceName)
                .computeIfAbsent(type, curType -> ruleCreator.apply(curType).orElse(null));
    }

    private Map<RibbonLoadbalancerType, AbstractLoadBalancerRule> getTargetServiceRuleMap(String serviceName) {
        return servicesRuleMap.computeIfAbsent(serviceName, key -> new ConcurrentHashMap<>());
    }

    private void setRule(ExecuteContext context) {
        if (!RuleManager.INSTANCE.isConfigured()) {
            return;
        }
        final Object rawLoadbalancerKey = context.getArguments()[0];
        if (rawLoadbalancerKey != null && !(rawLoadbalancerKey instanceof String)) {
            return;
        }
        String loadbalancerKey = (String) rawLoadbalancerKey;
        if (!isNeedUseSermantLb(loadbalancerKey)) {
            return;
        }
        final Optional<String> serviceNameOptional = getServiceName(context);
        if (!serviceNameOptional.isPresent()) {
            return;
        }
        String serviceName = serviceNameOptional.get();
        backUp(serviceName, (BaseLoadBalancer) context.getObject());
        final Optional<RibbonLoadbalancerType> targetType = RibbonLoadbalancerCache.INSTANCE
                .getTargetServiceLbType(serviceName);
        if (targetType.isPresent()) {
            doSet(context.getObject(), getTargetRule(serviceName, targetType.get()));
        } else {
            tryUseDefaultType(serviceName, context);
        }
    }

    private void tryUseDefaultType(String serviceName, ExecuteContext context) {
        final String defaultRule = config.getDefaultRule();
        if (defaultRule == null) {
            RibbonLoadbalancerCache.INSTANCE.put(serviceName, null);
            return;
        }
        final Optional<RibbonLoadbalancerType> ribbonLoadbalancerType = RibbonLoadbalancerType
                .matchLoadbalancer(defaultRule);
        if (ribbonLoadbalancerType.isPresent()) {
            final AbstractLoadBalancerRule rule = getTargetRule(serviceName, ribbonLoadbalancerType.get());
            doSet(context.getObject(), rule);
            RibbonLoadbalancerCache.INSTANCE.put(serviceName, ribbonLoadbalancerType.get());
        }
    }

    private boolean isNeedUseSermantLb(String loadbalancerKey) {
        if (loadbalancerKey == null || DEFAULT_RIBBON_LOADBALANCER_KEY.equals(loadbalancerKey)) {
            return true;
        }
        return config.isForceUseSermantLb();
    }

    /**
     * 获取服务名
     *
     * @param context 上下文
     * @return 服务名
     */
    private Optional<String> getServiceName(ExecuteContext context) {
        final Object object = context.getObject();
        if (object instanceof BaseLoadBalancer) {
            return Optional.ofNullable(((BaseLoadBalancer) object).getName());
        }
        return Optional.empty();
    }

    private void doSet(Object obj, AbstractLoadBalancerRule rule) {
        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) obj;
        if (rule == null || loadBalancer.getRule().getClass() == rule.getClass()) {
            // 如果原来的负载均衡器跟需要的一样，就不需要修改了，直接return，不影响原方法
            return;
        }
        loadBalancer.setRule(rule);
    }

    private void backUp(String serviceName, BaseLoadBalancer loadBalancer) {
        if (backUpMarks.contains(serviceName)) {
            return;
        }
        backUpMarks.add(serviceName);
        final IRule rule = loadBalancer.getRule();
        final Optional<RibbonLoadbalancerType> ribbonLoadbalancerType = RibbonLoadbalancerType
                .matchLoadbalancerByClazz(rule.getClass().getName());
        if (ribbonLoadbalancerType.isPresent()) {
            RibbonLoadbalancerCache.INSTANCE.backUpOriginType(serviceName, ribbonLoadbalancerType.get());
        } else {
            LOGGER.warning(String.format(Locale.ENGLISH, "Can not resolve rule [%s] when back up",
                    rule.getClass().getName()));
        }
    }
}
