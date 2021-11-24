/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.flowcontrol.adapte.cse.match;

import com.huawei.flowcontrol.adapte.cse.ResolverManager;
import com.huawei.flowcontrol.adapte.cse.entity.CseMatchRequest;
import com.huawei.flowcontrol.adapte.cse.resolver.RateLimitingRuleResolver;
import com.huawei.flowcontrol.adapte.cse.rule.RateLimitingRule;
import com.huawei.flowcontrol.util.FilterUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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

    public void tryEntry(HttpServletRequest request) {
        final CseMatchRequest cseRequest = buildRequest(request);
        // 匹配规则
        final MatchGroupResolver resolver = ResolverManager.INSTANCE.getResolver(MatchGroupResolver.CONFIG_KEY);
        final Map<String, BusinessMatcher> matchGroups = resolver.getRules();
        final RateLimitingRuleResolver rateLimitingRuleResolver = ResolverManager.INSTANCE.getResolver(RateLimitingRuleResolver.CONFIG_KEY);
        for (Map.Entry<String, BusinessMatcher> entry : matchGroups.entrySet()) {
            if (entry.getValue().match(cseRequest.getApiPath(), cseRequest.getHeaders(), cseRequest.getHttpMethod())) {
                final RateLimitingRule rule = rateLimitingRuleResolver.getRules().get(entry.getKey());
                System.out.println(rule.getName());
            }
        }
    }

    public CseMatchRequest buildRequest(HttpServletRequest request) {
        // 获取路径
        String apiPath = FilterUtil.filterTarget(request);
        // 获取请求头
        final Enumeration<String> headerNames = request.getHeaderNames();
        final Map<String, String> headers = new HashMap<String, String>();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        // 方法类型
        final String method = request.getMethod();
        return new CseMatchRequest(apiPath, headers, method);
    }
}
