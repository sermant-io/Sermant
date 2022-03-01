/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.common.adapte.cse.match;

import com.huawei.flowcontrol.common.adapte.cse.ResolverManager;
import com.huawei.flowcontrol.common.entity.RequestEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 匹配管理器
 *
 * @author zhouss
 * @since 2021-11-24
 */
public enum MatchManager {
    /**
     * 单例
     */
    INSTANCE;

    private static final int DEFAULT_BUSINESS_SIZE = 4;

    /**
     * 匹配所有业务场景
     *
     * @param request 请求信息
     * @return 匹配的业务场景
     */
    public Set<String> match(RequestEntity request) {
        return match(request, null);
    }

    /**
     * 匹配指定业务场景名
     *
     * @param request      请求信息
     * @param businessName 业务场景名
     * @return 匹配的业务场景
     */
    public Set<String> match(RequestEntity request, String businessName) {
        // 匹配规则
        final Map<String, BusinessMatcher> matchGroups = getMatchGroups(businessName);
        final Set<String> result = new HashSet<>(DEFAULT_BUSINESS_SIZE);
        for (Map.Entry<String, BusinessMatcher> entry : matchGroups.entrySet()) {
            if (entry.getValue().match(request.getApiPath(), request.getHeaders(), request.getMethod())) {
                if (!ResolverManager.INSTANCE.hasMatchedRule(entry.getKey())) {
                    continue;
                }

                // 资源名（业务场景名）
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private Map<String, BusinessMatcher> getMatchGroups(String businessName) {
        final MatchGroupResolver resolver = ResolverManager.INSTANCE.getResolver(MatchGroupResolver.CONFIG_KEY);
        final Map<String, BusinessMatcher> matchGroups = resolver.getRules();
        if (businessName == null) {
            return matchGroups;
        }
        final BusinessMatcher businessMatcher = matchGroups.get(businessName);
        if (businessMatcher == null) {
            return Collections.emptyMap();
        }
        return Collections.singletonMap(businessName, businessMatcher);
    }
}
