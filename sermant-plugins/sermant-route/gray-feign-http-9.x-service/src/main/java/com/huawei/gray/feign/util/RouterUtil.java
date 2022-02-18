/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.gray.feign.util;

import com.huawei.gray.feign.context.CurrentInstance;
import com.huawei.route.common.gray.addr.entity.Instances;
import com.huawei.route.common.gray.constants.GrayConstant;
import com.huawei.route.common.gray.label.LabelCache;
import com.huawei.route.common.gray.label.entity.CurrentTag;
import com.huawei.route.common.gray.label.entity.GrayConfiguration;
import com.huawei.route.common.gray.label.entity.Match;
import com.huawei.route.common.gray.label.entity.MatchRule;
import com.huawei.route.common.gray.label.entity.MatchStrategy;
import com.huawei.route.common.gray.label.entity.Route;
import com.huawei.route.common.gray.label.entity.Rule;
import com.huawei.route.common.gray.label.entity.ValueMatch;
import com.huawei.sermant.core.lubanops.bootstrap.utils.StringUtils;

import com.alibaba.fastjson.JSONObject;

import feign.Request;

import org.springframework.util.CollectionUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 灰度路由插件工具类
 *
 * @author lilai
 * @since 2021/10/30
 */
public class RouterUtil {
    private static final String HTTP = "http";

    private static final int HTTP_PORT = 80;

    private static final String HTTPS = "https";

    private static final int HTTPS_PORT = 443;

    private static final String COOKIE = "Cookie";

    private static final String COLON = ":";

    private RouterUtil() {
    }

    /**
     * 获取当前服务的ldc
     *
     * @return LDC
     */
    public static String getLdc() {
        GrayConfiguration grayConfiguration = LabelCache.getLabel(CurrentInstance.getInstance().getAppName());
        if (GrayConfiguration.isInValid(grayConfiguration)) {
            return GrayConstant.GRAY_DEFAULT_LDC;
        }
        CurrentTag currentTag = grayConfiguration.getCurrentTag();
        if (currentTag == null || StringUtils.isBlank(currentTag.getLdc())) {
            return GrayConstant.GRAY_DEFAULT_LDC;
        }
        return currentTag.getLdc();
    }

    /**
     * 根据header获取ldc
     *
     * @param headers headers
     * @return LDC
     */
    public static String getLdc(Map<String, Collection<String>> headers) {
        if (headers.containsKey(GrayConstant.GRAY_LDC)) {
            return new ArrayList<String>(headers.get(GrayConstant.GRAY_LDC)).get(0);
        } else {
            return getLdc();
        }
    }

    /**
     * 更换旧的URL中的IP和端口，
     *
     * @param host 新的请求地址
     * @param version 灰度后的版本
     * @param request feign请求
     * @return 是否更换成功
     */
    public static Request rebuildUrl(String host, String version, Request request) {
        if (StringUtils.isBlank(version) || request == null) {
            return request;
        }
        String[] arr = host.split(COLON);
        if (arr.length != 2) {
            return request;
        }
        final String newUrl = replaceUrl(request.url(), arr[0], Integer.parseInt(arr[1])).toString();
        Map<String, Collection<String>> headers = request.headers();

        // 由于headers有可能是一个UnmodifiableMap，所以只能new一个出来然后putAll
        Map<String, Collection<String>> headerMap = new HashMap<String, Collection<String>>(headers);
        if (!headerMap.containsKey(GrayConstant.GRAY_LDC)) {
            headerMap.put(GrayConstant.GRAY_LDC, Collections.singletonList(getLdc(headers)));
        }
        CurrentTag currentTag = new CurrentTag();
        currentTag.setVersion(version);
        currentTag.setLdc(getLdc(headers));
        headerMap.put(GrayConstant.GRAY_TAG, Collections.singletonList(JSONObject.toJSONString(currentTag)));

        return Request.create(request.method(), newUrl, headerMap, request.body(), request.charset());
    }

    /**
     * 使用目标ip与端口替换原始地址
     *
     * @param originUrl 原始地址
     * @param targetServiceIp 目标IP
     * @param port 目标端口
     * @return 替换后的地址
     */
    public static StringBuilder replaceUrl(String originUrl, String targetServiceIp, int port) {
        StringBuilder url = new StringBuilder();
        try {
            URL uri = new URL(originUrl);
            url.append(uri.getProtocol()).append("://").append(targetServiceIp);
            int targetPort;
            if (port == -1) {
                // 若无指定端口，则使用默认的443与80端口
                targetPort = uri.getPort() == -1 ? (StringUtils.equalsIgnoreCase(HTTPS, uri.getProtocol()) ? HTTPS_PORT
                    : HTTP_PORT) : uri.getPort();
            } else {
                targetPort = port;
            }

            // 附加端口与路径
            url.append(COLON).append(targetPort).append(uri.getPath());
            if (!StringUtils.isBlank(uri.getQuery())) {
                url.append("?").append(uri.getQuery());
            }
        } catch (MalformedURLException e) {
            return new StringBuilder(originUrl);
        }
        return url;
    }

    /**
     * 获取目标host
     *
     * @param instance 实例
     * @return host
     */
    public static String getTargetHost(Instances instance) {
        if (instance != null && instance.getIp() != null && instance.getPort() > 0) {
            return instance.getIp() + COLON + instance.getPort();
        }
        return null;
    }

    /**
     * 获取有效规则
     *
     * @param grayConfiguration 灰度配置
     * @param targetService 目标服务
     * @param path 请求路径
     * @return 有效规则
     */
    public static List<Rule> getValidRules(GrayConfiguration grayConfiguration, String targetService, String path) {
        if (GrayConfiguration.isInValid(grayConfiguration)) {
            return Collections.emptyList();
        }
        Map<String, List<Rule>> routeRule = grayConfiguration.getRouteRule();
        if (CollectionUtils.isEmpty(routeRule) || CollectionUtils.isEmpty(routeRule.get(targetService))) {
            return Collections.emptyList();
        }
        List<Rule> list = new ArrayList<Rule>();
        for (Rule rule : routeRule.get(targetService)) {
            if (!isValidRule(rule, CurrentInstance.getInstance().getAppName(), path)) {
                continue;
            }

            // 去掉无效的规则
            Map<String, List<MatchRule>> headerRules = rule.getMatch().getHeaders();
            if (headerRules != null) {
                removeInValidRules(headerRules);
                rule.getMatch().setHeaders(headerRules);
            }
            Map<String, List<MatchRule>> paramRules = rule.getMatch().getParameters();
            if (paramRules != null) {
                removeInValidRules(paramRules);
                rule.getMatch().setParameters(paramRules);
            }
            Map<String, List<MatchRule>> cookieRules = rule.getMatch().getCookie();
            if (cookieRules != null) {
                removeInValidRules(cookieRules);
                rule.getMatch().setCookie(cookieRules);
            }

            // 去掉无效的路由
            Iterator<Route> routeIterator = rule.getRoute().iterator();
            while (routeIterator.hasNext()) {
                if (isInValidRoute(routeIterator.next())) {
                    routeIterator.remove();
                }
            }
            list.add(rule);
        }
        Collections.sort(list, new Comparator<Rule>() {
            @Override
            public int compare(Rule o1, Rule o2) {
                return o1.getPrecedence() - o2.getPrecedence();
            }
        });
        return list;
    }

    private static void removeInValidRules(Map<String, List<MatchRule>> rules) {
        Iterator<Entry<String, List<MatchRule>>> headerRulesIterator = rules.entrySet().iterator();
        while (headerRulesIterator.hasNext()) {
            if (isInValidArgs(headerRulesIterator.next())) {
                headerRulesIterator.remove();
            }
        }
        for (List<MatchRule> matchRules : rules.values()) {
            Iterator<MatchRule> headerRuleIterator = matchRules.iterator();
            while (headerRuleIterator.hasNext()) {
                if (isInValidMatchRule(headerRuleIterator.next())) {
                    headerRuleIterator.remove();
                }
            }
        }
    }

    private static boolean isValidRule(Rule rule, String applicationName, String path) {
        if (rule == null) {
            return false;
        }
        Match match = rule.getMatch();
        if (match == null) {
            return false;
        }
        String source = match.getSource();
        if (!StringUtils.isBlank(source) && !source.equals(applicationName)) {
            return false;
        }
        String[] arr = match.getPath().split(":/");
        if (arr.length != 2) {
            return false;
        }
        String protocol = arr[0];
        if (!HTTP.equalsIgnoreCase(protocol) && !HTTPS.equalsIgnoreCase(protocol)) {
            return false;
        }
        if (!path.equals(arr[1])) {
            return false;
        }

        if (CollectionUtils.isEmpty(match.getHeaders()) && CollectionUtils.isEmpty(match.getParameters())
            && CollectionUtils.isEmpty(match.getCookie())) {
            return false;
        }
        return !CollectionUtils.isEmpty(rule.getRoute());
    }

    private static boolean isInValidArgs(Entry<String, List<MatchRule>> entry) {
        return StringUtils.isBlank(entry.getKey()) || CollectionUtils.isEmpty(entry.getValue());
    }

    private static boolean isInValidMatchRule(MatchRule matchRule) {
        return matchRule == null || matchRule.getValueMatch() == null || CollectionUtils
            .isEmpty(matchRule.getValueMatch().getValues()) || matchRule.getValueMatch().getMatchStrategy() == null;
    }

    private static boolean isInValidRoute(Route route) {
        return route == null || route.getTags() == null || StringUtils.isBlank(route.getTags().getVersion());
    }

    private static boolean isMatchHeaderRule(Map<String, List<MatchRule>> headerRules, boolean isFullMatch,
        Request request) {
        Map<String, Collection<String>> headers = request.headers();
        for (Entry<String, List<MatchRule>> entry : headerRules.entrySet()) {
            String key = entry.getKey();
            if (!headers.containsKey(key) || headers.get(key).size() == 0) {
                if (isFullMatch) {
                    return false;
                } else {
                    continue;
                }
            }
            List<MatchRule> headerMatchRuleList = entry.getValue();
            for (MatchRule matchRule : headerMatchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy headerMatchStrategy = valueMatch.getMatchStrategy();
                String arg = new ArrayList<String>(headers.get(key)).get(0);
                if (!isFullMatch && headerMatchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果不是全匹配，且匹配了一个，那么直接return
                    return true;
                }
                if (isFullMatch && !headerMatchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果是全匹配，且有一个不匹配，则继续下一个规则
                    return false;
                }
            }
        }

        // 执行到此处，如果是全匹配，则匹配成功，如果不是全匹配，则没有匹配到
        return isFullMatch;
    }

    private static boolean isMatchParamRule(Map<String, List<MatchRule>> paramRules, boolean isFullMatch,
        Request request) {
        Map<String, String> paramMap = new HashMap<String, String>();
        try {
            URL url = new URL(request.url());
            String queryString = url.getQuery();
            List<String> paramList = new ArrayList<String>(Arrays.asList(queryString.split("&")));
            if (transformParam(paramList, paramMap)) {
                return false;
            }
        } catch (MalformedURLException e) {
            return false;
        }

        return matchResultForParamAndCookie(paramRules, isFullMatch, paramMap);
    }

    private static boolean isMatchCookieRule(Map<String, List<MatchRule>> cookieRules, boolean isFullMatch,
        Request request) {
        List<String> cookieList = new ArrayList<String>(request.headers().get(COOKIE));
        Map<String, String> cookieMap = new HashMap<String, String>();
        if (transformParam(cookieList, cookieMap)) {
            return false;
        }
        return matchResultForParamAndCookie(cookieRules, isFullMatch, cookieMap);
    }

    private static boolean transformParam(List<String> cookieList, Map<String, String> cookieMap) {
        if (cookieList.size() == 0) {
            return true;
        }
        for (String str : cookieList) {
            String[] kv = str.split("=");
            if (kv.length != 2) {
                continue;
            }
            cookieMap.put(kv[0], kv[1]);
        }
        return false;
    }

    private static boolean matchResultForParamAndCookie(Map<String, List<MatchRule>> rules, boolean isFullMatch,
        Map<String, String> paramMap) {
        for (Entry<String, List<MatchRule>> entry : rules.entrySet()) {
            String key = entry.getKey();
            if (!paramMap.containsKey(key)) {
                if (isFullMatch) {
                    return false;
                } else {
                    continue;
                }
            }
            List<MatchRule> matchRuleList = entry.getValue();
            for (MatchRule matchRule : matchRuleList) {
                ValueMatch valueMatch = matchRule.getValueMatch();
                List<String> values = valueMatch.getValues();
                MatchStrategy matchStrategy = valueMatch.getMatchStrategy();
                String arg = paramMap.get(key);
                if (!isFullMatch && matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果不是全匹配，且匹配了一个，那么直接return
                    return true;
                }
                if (isFullMatch && !matchStrategy.isMatch(values, arg, matchRule.isCaseInsensitive())) {
                    // 如果是全匹配，且有一个不匹配，则继续下一个规则
                    return false;
                }
            }
        }

        // 执行到此处，如果是全匹配，则匹配成功，如果不是全匹配，则没有匹配到
        return isFullMatch;
    }

    /**
     * 获取路由
     *
     * @param list 规则列表
     * @param request 请求
     * @return 路由
     */
    public static List<Route> getRoutes(List<Rule> list, Request request) {
        // 支持headers，parameters和cookie三种参数匹配方式且允许同时配置，全匹配应满足所有三种匹配方式规则
        for (Rule rule : list) {
            Match match = rule.getMatch();
            boolean isFullMatch = match.isFullMatch();

            // headers 参数匹配
            Map<String, List<MatchRule>> headerRules = match.getHeaders();
            boolean isMatchByHeader = true;
            if (headerRules != null) {
                isMatchByHeader = isMatchHeaderRule(headerRules, isFullMatch, request);
            }

            // parameters 参数匹配
            Map<String, List<MatchRule>> paramRules = match.getParameters();
            boolean isMatchByParam = true;
            if (paramRules != null) {
                isMatchByParam = isMatchParamRule(paramRules, isFullMatch, request);
            }

            // cookie 参数匹配
            Map<String, List<MatchRule>> cookieRules = match.getCookie();
            boolean isMatchByCookie = true;
            if (cookieRules != null) {
                isMatchByCookie = isMatchCookieRule(cookieRules, isFullMatch, request);
            }

            if (headerRules == null && paramRules == null && cookieRules == null) {
                continue;
            }

            // 全匹配需要三种配置都匹配上
            if (isFullMatch && isMatchByHeader && isMatchByParam && isMatchByCookie) {
                return rule.getRoute();
            }

            // 非全匹配只要其中一种规则匹配上即可
            if (!isFullMatch && (isMatchByHeader || isMatchByParam || isMatchByCookie)) {
                return rule.getRoute();
            }
        }
        return null;
    }
}
