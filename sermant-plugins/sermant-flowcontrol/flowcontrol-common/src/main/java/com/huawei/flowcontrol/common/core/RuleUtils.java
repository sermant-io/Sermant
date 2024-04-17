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

package com.huawei.flowcontrol.common.core;

import com.huawei.flowcontrol.common.core.match.MatchManager;
import com.huawei.flowcontrol.common.core.resolver.AbstractResolver;
import com.huawei.flowcontrol.common.entity.HttpRequestEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * rules get tool classes
 *
 * @author zhouss
 * @since 2022-07-25
 */
public class RuleUtils {
    private RuleUtils() {
    }

    /**
     * acquisition rule
     *
     * @param businessName business name
     * @param resolverKey resolver key
     * @param ruleType rule type
     * @param <T> rule type
     * @return rule
     */
    public static <T> T getRule(String businessName, String resolverKey, Class<T> ruleType) {
        final AbstractResolver<?> resolver = ResolverManager.INSTANCE.getResolver(resolverKey);
        return (T)resolver.getRules().get(businessName);
    }

    /**
     * Match the fetch rules according to the request body
     *
     * @param requestEntity request entity
     * @param resolverKey resolver key
     * @param ruleType rule type
     * @param <T> rule type
     * @return List-Rule
     */
    public static <T> List<T> getRule(HttpRequestEntity requestEntity, String resolverKey, Class<T> ruleType) {
        final Set<String> businessNames = MatchManager.INSTANCE.matchWithCache(requestEntity);
        if (businessNames.isEmpty()) {
            return Collections.emptyList();
        }
        final List<T> result = new ArrayList<>(businessNames.size());
        for (String businessName : businessNames) {
            final T rule = getRule(businessName, resolverKey, ruleType);
            if (rule != null) {
                result.add(rule);
            }
        }
        return result;
    }
}
