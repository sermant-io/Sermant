/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.dubbo.service;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.request.RequestHeader;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.FlowContextUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.EnabledStrategy;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.utils.RuleUtils;
import com.huaweicloud.sermant.router.dubbo.cache.DubboCache;
import com.huaweicloud.sermant.router.dubbo.strategy.RuleStrategyHandler;
import com.huaweicloud.sermant.router.dubbo.utils.DubboReflectUtils;
import com.huaweicloud.sermant.router.dubbo.utils.RouteUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AbstractDirectory的service
 *
 * @author provenceee
 * @since 2021-11-24
 */
public class AbstractDirectoryServiceImpl implements AbstractDirectoryService {
    // dubbo请求参数中是否为consumer的key值
    private static final String CONSUMER_KEY = "side";

    // dubbo请求参数中接口名的key值
    private static final String INTERFACE_KEY = "interface";

    // dubbo请求参数中是否为consumer的value值
    private static final String CONSUMER_VALUE = "consumer";

    private static final String DASH = "-";

    private static final String POINT = ".";

    private final RouterConfig routerConfig;

    // 用于过滤实例的tags集合，value为null，代表含有该标签的实例全部过滤，不判断value值
    private final Map<String, String> allMismatchTags;

    /**
     * 构造方法
     */
    public AbstractDirectoryServiceImpl() {
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
        allMismatchTags = new HashMap<>();
        for (String requestTag : routerConfig.getRequestTags()) {
            // dubbo会把key中的"-"替换成"."
            allMismatchTags.put(requestTag.replace(DASH, POINT), null);
        }

        // 所有实例都含有version，所以不能存入null值
        allMismatchTags.remove(RouterConstant.DUBBO_VERSION_KEY);
    }

    /**
     * 筛选标签invoker
     *
     * @param registryDirectory RegistryDirectory
     * @param arguments 参数
     * @param result invokers
     * @return invokers
     * @see com.alibaba.dubbo.registry.integration.RegistryDirectory
     * @see org.apache.dubbo.registry.integration.RegistryDirectory
     * @see com.alibaba.dubbo.rpc.Invoker
     * @see org.apache.dubbo.rpc.Invoker
     */
    @Override
    public Object selectInvokers(Object registryDirectory, Object[] arguments, Object result) {
        if (arguments == null || arguments.length == 0) {
            return result;
        }
        if (!(result instanceof List<?>)) {
            return result;
        }
        Object invocation = arguments[0];
        putAttachment(invocation);
        List<Object> invokers = (List<Object>) result;
        Map<String, String> queryMap = DubboReflectUtils.getQueryMap(registryDirectory);
        if (CollectionUtils.isEmpty(queryMap)) {
            return invokers;
        }
        if (!CONSUMER_VALUE.equals(queryMap.get(CONSUMER_KEY))) {
            return invokers;
        }
        String serviceInterface = queryMap.get(INTERFACE_KEY);
        String targetService = DubboCache.INSTANCE.getApplication(serviceInterface);
        if (StringUtils.isBlank(targetService)) {
            return invokers;
        }
        if (!shouldHandle(invokers)) {
            return invokers;
        }
        List<Object> targetInvokers;
        if (routerConfig.isUseRequestRouter()) {
            targetInvokers = getTargetInvokersByRequest(targetService, invokers, invocation);
        } else {
            targetInvokers = getTargetInvokersByRules(invokers, invocation, queryMap, targetService, serviceInterface);
        }
        return getZoneInvokers(targetService, targetInvokers);
    }

    /**
     * 解析下dubbo的附件信息
     *
     * @param invocation dubbo的invocation
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    private Map<String, Object> parseAttachments(Object invocation) {
        Map<String, Object> attachments = DubboReflectUtils.getAttachments(invocation);
        return FlowContextUtils.decodeAttachments(attachments);
    }

    /**
     * 获取dubbo应用 group
     *
     * @param queryMap queryMap
     * @return 值
     */
    private String getGroup(Map<String, String> queryMap) {
        String group = queryMap.get(RouterConstant.DUBBO_GROUP_KEY);
        return group == null ? "" : group;
    }

    /**
     * 获取dubbo 应用 version
     *
     * @param queryMap queryMap
     * @return 值
     */
    private String getVersion(Map<String, String> queryMap) {
        String version = queryMap.get(RouterConstant.DUBBO_VERSION_KEY);
        return version == null ? "" : version;
    }

    private List<Object> getZoneInvokers(String targetService, List<Object> invokers) {
        EnabledStrategy strategy = ConfigCache.getEnabledStrategy(RouterConstant.DUBBO_CACHE_NAME);
        if (shouldHandle(invokers) && routerConfig.isEnabledDubboZoneRouter() && strategy.getStrategy()
            .isMatch(strategy.getValue(), targetService)) {
            return RuleStrategyHandler.INSTANCE.getZoneInvokers(targetService, invokers, routerConfig.getZone());
        }
        return invokers;
    }

    private boolean shouldHandle(List<Object> invokers) {
        // 实例数大于1才能路由
        return invokers != null && invokers.size() > 1;
    }

    private void putAttachment(Object invocation) {
        Map<String, Object> attachments = DubboReflectUtils.getAttachmentsByInvocation(invocation);
        if (attachments != null) {
            RequestHeader requestHeader = ThreadLocalUtils.getRequestHeader();
            if (requestHeader != null) {
                requestHeader.getHeader().forEach((key, value) -> attachments.putIfAbsent(key, value.get(0)));
            }
        }
    }

    private List<Object> getTargetInvokersByRules(List<Object> invokers, Object invocation,
        Map<String, String> queryMap, String targetService, String serviceInterface) {
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        if (RouterConfiguration.isInValid(configuration)) {
            return invokers;
        }
        String interfaceName = getGroup(queryMap) + "/" + serviceInterface + POINT
            + DubboReflectUtils.getMethodName(invocation) + ":" + getVersion(queryMap);
        List<Rule> rules = RuleUtils
            .getRules(configuration, targetService, interfaceName, DubboCache.INSTANCE.getAppName());
        List<Route> routes = RouteUtils.getRoutes(rules, DubboReflectUtils.getArguments(invocation),
            parseAttachments(invocation));
        if (!CollectionUtils.isEmpty(routes)) {
            return RuleStrategyHandler.INSTANCE.getMatchInvokers(targetService, invokers, routes);
        }
        return RuleStrategyHandler.INSTANCE
            .getMismatchInvokers(targetService, invokers, RuleUtils.getTags(rules, true), true);
    }

    private List<Object> getTargetInvokersByRequest(String targetName, List<Object> invokers, Object invocation) {
        Map<String, Object> attachments = parseAttachments(invocation);
        List<String> requestTags = routerConfig.getRequestTags();
        if (CollectionUtils.isEmpty(requestTags)) {
            return invokers;
        }

        // 用于匹配实例的tags集合
        Map<String, String> tags = new HashMap<>();

        // 用于过滤实例的tags集合，value为null，代表含有该标签的实例全部过滤，不判断value值
        Map<String, String> mismatchTags = new HashMap<>();
        for (String key : attachments.keySet()) {
            if (!requestTags.contains(key)) {
                continue;
            }
            String replaceDashKey = key;
            if (replaceDashKey.contains(DASH)) {
                // dubbo会把key中的"-"替换成"."
                replaceDashKey = replaceDashKey.replace(DASH, POINT);
            }
            mismatchTags.put(replaceDashKey, null);
            String value = Optional.ofNullable(attachments.get(key)).map(String::valueOf).orElse(null);
            if (StringUtils.isExist(value)) {
                tags.put(replaceDashKey, value);
            }
        }
        if (StringUtils.isExist(tags.get(RouterConstant.DUBBO_VERSION_KEY))) {
            mismatchTags.put(RouterConstant.DUBBO_VERSION_KEY, tags.get(RouterConstant.DUBBO_VERSION_KEY));
        } else {
            // 所有实例都含有version，所以不能存入null值
            mismatchTags.remove(RouterConstant.DUBBO_VERSION_KEY);
        }
        boolean isReturnAllInstancesWhenMismatch = false;
        if (CollectionUtils.isEmpty(mismatchTags)) {
            mismatchTags = allMismatchTags;
            isReturnAllInstancesWhenMismatch = true;
        }
        List<Object> result = RuleStrategyHandler.INSTANCE.getMatchInvokersByRequest(targetName, invokers, tags);
        if (CollectionUtils.isEmpty(result)) {
            result = RuleStrategyHandler.INSTANCE.getMismatchInvokers(targetName, invokers,
                Collections.singletonList(mismatchTags), isReturnAllInstancesWhenMismatch);
        }
        return result;
    }
}