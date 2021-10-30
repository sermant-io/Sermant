/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2022. All rights reserved.
 */

package com.huawei.route.server.rules;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.huawei.route.server.constants.RouteConstants;
import com.huawei.route.server.entity.Tag;
import com.huawei.route.server.rules.define.TargetServiceRule;
import com.huawei.route.server.rules.define.WeightRule;
import com.huawei.route.server.rules.define.key.KeyPair;
import com.huawei.route.server.rules.define.matcher.DubboTypeMatcher;
import com.huawei.route.server.rules.define.matcher.Matcher;
import com.huawei.route.server.rules.define.matcher.ReferMatcher;
import com.huawei.route.server.rules.define.matcher.TypeMatcher;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 实例标签配置
 *
 * @author zhouss
 * @since 2021-10-23
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InstanceTagConfiguration extends BaseConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceTagConfiguration.class);

    /**
     * 路由规则
     */
    private KeyPair<String, List<TargetServiceRule>> routeRule;

    /**
     * 规则转换
     *
     * @param rawRule 标签规则字符串
     * @return 转换后字符串
     */
    public static InstanceTagConfiguration convert(String rawRule) {
        if (StringUtils.isEmpty(rawRule)) {
            return null;
        }
        final JSONObject routeRuleJson;
        try {
            routeRuleJson = JSONObject.parseObject(rawRule);
        } catch (JSONException e) {
            LOGGER.warn("convert raw route rule to entity failed", e);
            return null;
        }
        final InstanceTagConfiguration configuration = new InstanceTagConfiguration();
        try {
            // 基础值解析
            resolveBaseValue(configuration, routeRuleJson);
            // 解析路由规则
            resolveRouteRule(configuration, routeRuleJson);
        } catch (Exception e) {
            LOGGER.warn("invalid tag configuration! reason: [{}]", e.getMessage());
            return null;
        }
        return configuration;
    }

    private static void resolveBaseValue(InstanceTagConfiguration configuration, JSONObject routeRuleJson) {
        final Boolean isValid = routeRuleJson.getBoolean("isValid");
        configuration.setValid(isValid != null && isValid);
        final Boolean isEntrance = routeRuleJson.getBoolean("isEntrance");
        configuration.setEntrance(isEntrance != null && isEntrance);
        final String currentTag = routeRuleJson.getString("currentTag");
        if (StringUtils.isNotEmpty(currentTag)) {
            configuration.setCurrentTag(JSONObject.parseObject(currentTag, Tag.class));
        }
    }

    /**
     * 解析路由规则
     *
     * @param configuration 待转换标签配置
     * @param routeRuleJson 路由规则
     */
    private static void resolveRouteRule(InstanceTagConfiguration configuration, JSONObject routeRuleJson) {
        final JSONObject routeRule = routeRuleJson.getJSONObject("routeRule");
        if (CollectionUtils.isEmpty(routeRule)) {
            return;
        }
        final KeyPair<String, List<TargetServiceRule>> routePairs = new KeyPair<>();
        for (Map.Entry<String, Object> entry : routeRule.entrySet()) {
            final String targetServiceName = entry.getKey();
            if (StringUtils.isEmpty(targetServiceName)) {
                continue;
            }
            final JSONArray jsonArray = JSONObject.parseArray(entry.getValue().toString());
            if (CollectionUtils.isEmpty(jsonArray)) {
                continue;
            }
            List<TargetServiceRule> targetServiceRules = new ArrayList<>();
            for (int i = 0, size = jsonArray.size(); i < size; i++) {
                final JSONObject jsonObject = jsonArray.getJSONObject(i);
                final TargetServiceRule serviceRule = new TargetServiceRule();
                serviceRule.setPrecedence(jsonObject.getInteger("precedence"));
                serviceRule.setMatch(getMatcher(jsonObject.getJSONObject("match")));
                serviceRule.setRoute(getRouteRule(jsonObject.getJSONArray("route")));
                if (!serviceRule.isValid()) {
                    LOGGER.warn("invalid service rule: {}, it will be skipped", jsonObject.toJSONString());
                    continue;
                }
                targetServiceRules.add(serviceRule);
            }
            if (routePairs.get(targetServiceName) != null) {
                LOGGER.warn("duplicate service {} route rule configuration, it will be replaced by the latter",
                        targetServiceName);
            }
            routePairs.put(targetServiceName, targetServiceRules);
        }
        configuration.setRouteRule(routePairs);
    }

    private static List<WeightRule> getRouteRule(JSONArray route) {
        if (route == null) {
            return null;
        }
        return route.toJavaList(WeightRule.class);
    }

    private static Matcher getMatcher(JSONObject match) {
        if (match == null) {
            return null;
        }
        final String refer = match.getString("refer");
        if (refer != null) {
            return JSONObject.parseObject(match.toJSONString(), ReferMatcher.class);
        }
        final String path = match.getString("path");
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        if (StringUtils.startsWithIgnoreCase(path, RouteConstants.DUBBO_PROTOCOL_PREFIX)) {
            return JSONObject.parseObject(match.toJSONString(), DubboTypeMatcher.class);
        } else {
            return JSONObject.parseObject(match.toJSONString(), TypeMatcher.class);
        }
    }
}
