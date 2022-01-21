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
import com.huawei.flowcontrol.common.adapte.cse.entity.CseMatchRequest;
import com.huawei.flowcontrol.common.entity.RequestEntity;

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

    /**
     * 从dubbo attachment获取version版本
     */
    public static final String DUBBO_ATTACHMENT_VERSION = "version";

    /**
     * 匹配所有的业务场景
     *
     * @param request http请求
     * @return 匹配的所有业务场景
     */
    public Set<String> match(RequestEntity request) {
        return match(buildRequest(request));
    }

    /**
     * 匹配业务场景
     *
     * @param cseRequest 请求信息
     * @return 匹配的业务场景
     */
    public Set<String> match(CseMatchRequest cseRequest) {
        // 匹配规则
        final MatchGroupResolver resolver = ResolverManager.INSTANCE.getResolver(MatchGroupResolver.CONFIG_KEY);
        final Map<String, BusinessMatcher> matchGroups = resolver.getRules();
        final Set<String> result = new HashSet<String>();
        for (Map.Entry<String, BusinessMatcher> entry : matchGroups.entrySet()) {
            if (entry.getValue().match(cseRequest.getApiPath(), cseRequest.getHeaders(), cseRequest.getHttpMethod())) {
                if (!ResolverManager.INSTANCE.hasMatchedRule(entry.getKey())) {
                    continue;
                }

                // 资源名（业务场景名）
                result.add(entry.getKey());
            }
        }
        return result;
    }

    private CseMatchRequest buildRequest(RequestEntity entity) {
        return new CseMatchRequest(entity.getApiPath(), entity.getHeaders(), entity.getMethod());
    }
}
