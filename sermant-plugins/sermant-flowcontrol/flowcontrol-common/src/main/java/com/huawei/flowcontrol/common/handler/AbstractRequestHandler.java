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
import com.huawei.flowcontrol.common.adapte.cse.match.MatchManager;
import com.huawei.flowcontrol.common.adapte.cse.resolver.AbstractResolver;
import com.huawei.flowcontrol.common.adapte.cse.rule.AbstractRule;
import com.huawei.flowcontrol.common.entity.RequestEntity;

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

    protected AbstractRequestHandler() {
        registerConfigListener();
    }

    private void registerConfigListener() {
        ResolverManager.INSTANCE.registerListener(configKey(), (updateKey, rules) -> handlers.remove(updateKey));
    }

    /**
     * 获取指定请求处理器
     *
     * @param request 请求信息
     * @return handler
     */
    public List<H> getHandlers(RequestEntity request) {
        final Set<String> businessNames = MatchManager.INSTANCE.match(request);
        if (businessNames.isEmpty()) {
            return Collections.emptyList();
        }
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
     * @param rule 匹配的解析规则
     * @return handler
     */
    protected abstract H createProcessor(String businessName, R rule);

    /**
     * 获取配置键
     *
     * @return 配置键， 用于注册配置监听器
     */
    protected abstract String configKey();
}
