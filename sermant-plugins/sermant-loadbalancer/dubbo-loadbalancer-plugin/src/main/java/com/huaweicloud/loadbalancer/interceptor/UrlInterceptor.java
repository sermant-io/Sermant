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

import com.huaweicloud.loadbalancer.cache.DubboLoadbalancerCache;
import com.huaweicloud.loadbalancer.config.DubboLoadbalancerType;
import com.huaweicloud.loadbalancer.config.LbContext;
import com.huaweicloud.loadbalancer.config.LoadbalancerConfig;
import com.huaweicloud.loadbalancer.rule.LoadbalancerRule;
import com.huaweicloud.loadbalancer.rule.RuleManager;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * URL增强类
 *
 * @author provenceee
 * @since 2022-01-20
 */
public class UrlInterceptor extends AbstractInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String LOAD_BALANCE_KEY = "loadbalance";

    /**
     * 下游服务名参数
     */
    private static final String REMOTE_APPLICATION = "remote.application";

    private final LoadbalancerConfig config;

    /**
     * 构造方法
     */
    public UrlInterceptor() {
        config = PluginConfigManager.getPluginConfig(LoadbalancerConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        LbContext.INSTANCE.setCurLoadbalancerType(LbContext.LOADBALANCER_DUBBO);
        Object[] arguments = context.getArguments();
        if (arguments != null && arguments.length > 1 && LOAD_BALANCE_KEY.equals(arguments[1])) {
            // 如果为empty，继续执行原方法，即使用宿主的负载均衡策略
            // 如果不为empty，则使用返回的type并跳过原方法
            getType(context).ifPresent(context::skip);
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private Optional<String> getType(ExecuteContext context) {
        if (config == null || !RuleManager.INSTANCE.isConfigured()) {
            // 没有配置的情况下return empty
            return Optional.empty();
        }
        return getRemoteApplication(context).flatMap(application -> matchLoadbalancerType(application)
                .map(loadbalancerType -> loadbalancerType.name().toUpperCase(Locale.ROOT)));
    }

    private Optional<DubboLoadbalancerType> matchLoadbalancerType(String application) {
        final DubboLoadbalancerType cacheType = DubboLoadbalancerCache.INSTANCE.getNewCache()
                .get(application);
        if (cacheType != null) {
            return Optional.of(cacheType);
        }
        final Optional<LoadbalancerRule> targetServiceRule = RuleManager.INSTANCE
                .getTargetServiceRule(application);
        if (!targetServiceRule.isPresent()) {
            return Optional.empty();
        }
        final Optional<DubboLoadbalancerType> dubboLoadbalancerType = DubboLoadbalancerType
                .matchLoadbalancer(targetServiceRule.get().getRule());
        dubboLoadbalancerType.ifPresent(type -> DubboLoadbalancerCache.INSTANCE.getNewCache().put(application, type));
        return dubboLoadbalancerType;
    }

    private Optional<String> getRemoteApplication(ExecuteContext context) {
        final Object object = context.getObject();
        final Optional<Object> getParameter = ReflectUtils
                .invokeMethod(object, "getParameter", new Class[]{String.class},
                        new Object[]{REMOTE_APPLICATION});
        return getParameter.map(result -> (String) result);
    }
}
