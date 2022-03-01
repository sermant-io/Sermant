/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.common.handler;

import com.huawei.flowcontrol.common.adapte.cse.ResolverManager;
import com.huawei.flowcontrol.common.adapte.cse.match.MatchGroupResolver;
import com.huawei.flowcontrol.common.adapte.cse.match.MatchManager;
import com.huawei.flowcontrol.common.adapte.cse.resolver.AbstractResolver;
import com.huawei.flowcontrol.common.adapte.cse.resolver.listener.ConfigUpdateListener;
import com.huawei.flowcontrol.common.adapte.cse.rule.AbstractRule;
import com.huawei.flowcontrol.common.entity.RequestEntity;
import com.huawei.flowcontrol.common.handler.listener.HandlerRequestListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 拦截请求处理器
 *
 * @param <H> 处理器 对应 resilience4j个处理器
 * @param <R> resolver解析规则
 * @author zhouss
 * @since 2022-01-22
 */
public abstract class AbstractRequestHandler<H, R extends AbstractRule> {
    /**
     * 处理器缓存 map 业务场景名, 处理器
     */
    private final Map<String, H> handlers = new ConcurrentHashMap<>();

    /**
     * 匹配的业务场景缓存
     */
    private final Map<RequestEntity, Set<String>> businessCache = new ConcurrentHashMap<>();

    /**
     * 请求配置监听列表
     */
    private final List<HandlerRequestListener> configListeners = new ArrayList<>();

    protected AbstractRequestHandler() {
        registerConfigListener();
    }

    private void registerConfigListener() {
        ResolverManager.INSTANCE.registerListener(configKey(), new HandlerConfigListener(false));
        ResolverManager.INSTANCE.registerListener(MatchGroupResolver.CONFIG_KEY, new HandlerConfigListener(true));
    }

    /**
     * 注册处理器监听器
     *
     * @param listener 监听器
     */
    public void registerListener(HandlerRequestListener listener) {
        if (listener != null) {
            configListeners.add(listener);
        }
    }

    /**
     * 获取指定请求处理器
     *
     * @param request 请求信息
     * @return handler
     */
    public List<H> getHandlers(RequestEntity request) {
        final Set<String> businessNames = businessCache
            .computeIfAbsent(request, fn -> MatchManager.INSTANCE.match(request));
        if (businessNames.isEmpty()) {
            return Collections.emptyList();
        }
        return createHandlers(businessNames);
    }

    private List<H> createHandlers(Set<String> businessNames) {
        return businessNames.stream()
            .map(businessName -> handlers.computeIfAbsent(businessName, fn -> create(businessName)))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private H create(String businessName) {
        final AbstractResolver<?> resolver = ResolverManager.INSTANCE.getResolver(configKey());
        final R rule = (R) resolver.getRules().get(businessName);
        if (rule == null) {
            return null;
        }
        return createProcessor(businessName, rule);
    }

    /**
     * 创建处理器
     *
     * @param businessName 业务场景名
     * @param rule         匹配的解析规则
     * @return handler
     */
    protected abstract H createProcessor(String businessName, R rule);

    /**
     * 获取配置键
     *
     * @return 配置键， 用于注册配置监听器
     */
    protected abstract String configKey();

    class HandlerConfigListener implements ConfigUpdateListener<R> {
        private final boolean isMatchGroupListener;

        HandlerConfigListener(boolean isMatchGroupListener) {
            this.isMatchGroupListener = isMatchGroupListener;
        }

        @Override
        public void notify(String updateKey, Map<String, R> rules) {
            if (!isMatchGroupListener) {
                handlers.remove(updateKey);
            }

            // 更新业务场景，重新进行匹配
            businessCache.forEach((entity, businessNames) -> {
                boolean isNeedNotify = false;
                final Set<String> match = MatchManager.INSTANCE.match(entity, updateKey);
                if (match.isEmpty()) {
                    if (businessNames.contains(updateKey)) {
                        // 已经不匹配场景, 执行移除
                        businessNames.remove(updateKey);
                        isNeedNotify = true;
                    }
                } else {
                    // 匹配当前场景
                    businessNames.add(updateKey);
                    isNeedNotify = true;
                }

                // 更新数据
                businessCache.put(entity, businessNames);
                if (isNeedNotify) {
                    // 通知关联该业务场景的请求配置更新各自缓存
                    configListeners.forEach(listener -> listener.notify(entity, updateKey));
                }
            });
        }
    }
}
