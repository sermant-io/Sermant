/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huawei.flowcontrol.common.core.match;

import com.huawei.flowcontrol.common.core.ResolverManager;
import com.huawei.flowcontrol.common.entity.RequestEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * match manager
 *
 * @author zhouss
 * @since 2021-11-24
 */
public enum MatchManager {
    /**
     * singleton
     */
    INSTANCE;

    private static final int DEFAULT_BUSINESS_SIZE = 4;

    private final MatchedCache matchedCache = new MatchedCache();

    private MatchGroupResolver matchGroupResolver;

    /**
     * matches all service scenarios
     *
     * @param request request-information
     * @return matching service scenarios
     */
    public Set<String> matchWithCache(RequestEntity request) {
        return matchWithCache(request, null);
    }

    /**
     * matches the specified service scenario name
     *
     * @param request request_information
     * @param businessName service scenario name
     * @return matching service scenarios
     */
    public Set<String> matchWithCache(RequestEntity request, String businessName) {
        final Set<String> businesses = matchedCache.getDelegate().get(request);
        if (businesses != null) {
            return businesses;
        }
        final Set<String> result = match(request, businessName);
        matchedCache.getDelegate().put(request, result);
        return result;
    }

    /**
     * matches the specified service scenario name
     *
     * @param request request-information
     * @param businessName service scenario name
     * @return matching service scenarios
     */
    public Set<String> match(RequestEntity request, String businessName) {
        // matchingRule
        final Map<String, BusinessMatcher> matchGroups = getMatchGroups(businessName);
        final Set<String> result = new HashSet<>(DEFAULT_BUSINESS_SIZE);
        for (Map.Entry<String, BusinessMatcher> entry : matchGroups.entrySet()) {
            if (entry.getValue().match(request)) {
                if (!ResolverManager.INSTANCE.hasMatchedRule(entry.getKey())) {
                    continue;
                }

                // resourceName（service scenario name）
                result.add(entry.getKey());
            }
        }
        return result.isEmpty() ? Collections.emptySet() : result;
    }

    private Map<String, BusinessMatcher> getMatchGroups(String businessName) {
        final Map<String, BusinessMatcher> matchGroups = getMatchGroupResolver().getRules();
        if (businessName == null) {
            return matchGroups;
        }
        final BusinessMatcher businessMatcher = matchGroups.get(businessName);
        if (businessMatcher == null) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap(businessName, businessMatcher);
    }

    private MatchGroupResolver getMatchGroupResolver() {
        if (matchGroupResolver == null) {
            matchGroupResolver = ResolverManager.INSTANCE.getResolver(MatchGroupResolver.CONFIG_KEY);
        }
        return matchGroupResolver;
    }

    public MatchedCache getMatchedCache() {
        return matchedCache;
    }
}
