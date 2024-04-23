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

package io.sermant.flowcontrol.common.handler;

import io.sermant.flowcontrol.common.core.ResolverManager;
import io.sermant.flowcontrol.common.core.match.MatchManager;
import io.sermant.flowcontrol.common.core.resolver.AbstractResolver;
import io.sermant.flowcontrol.common.core.rule.AbstractRule;
import io.sermant.flowcontrol.common.entity.RequestEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * interceptRequestHandler
 *
 * @param <H> processor indicates resilience4j processors
 * @param <R> resolver analytic rule
 * @author zhouss
 * @since 2022-01-22
 */
public abstract class AbstractRequestHandler<H, R extends AbstractRule> {
    /**
     * Handler cache
     */
    private final Map<String, Optional<H>> handlers = new ConcurrentHashMap<>();

    /**
     * construction method
     */
    protected AbstractRequestHandler() {
        registerConfigListener();
    }

    private void registerConfigListener() {
        ResolverManager.INSTANCE.registerListener(configKey(), (updateKey, rules) -> handlers.remove(updateKey));
    }

    /**
     * gets the specified request handler
     *
     * @param request request information
     * @return handler
     */
    public List<H> getHandlers(RequestEntity request) {
        final Set<String> businessNames = MatchManager.INSTANCE.matchWithCache(request);
        if (businessNames.isEmpty()) {
            return Collections.emptyList();
        }
        return createOrGetHandlers(businessNames);
    }

    /**
     * create handler
     *
     * @param businessNames matched service name
     * @return handler
     */
    public List<H> createOrGetHandlers(Set<String> businessNames) {
        return businessNames.stream()
                .map(businessName -> handlers.computeIfAbsent(businessName, fn -> create(businessName)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<H> create(String businessName) {
        final AbstractResolver<?> resolver = ResolverManager.INSTANCE.getResolver(configKey());
        final R rule = (R) resolver.getRules().get(businessName);
        if (rule == null) {
            return Optional.empty();
        }
        return createProcessor(businessName, rule);
    }

    /**
     * create handler
     *
     * @param businessName service scenario name
     * @param rule matching resolution rules
     * @return handler
     */
    protected abstract Optional<H> createProcessor(String businessName, R rule);

    /**
     * get configuration key
     *
     * @return Configuration key, used to register the configuration listener
     * @since 2022-03-22
     */
    protected abstract String configKey();
}
