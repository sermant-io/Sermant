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

import java.util.List;
import java.util.Map;

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

    private final RouterConfig routerConfig;

    /**
     * 构造方法
     */
    public AbstractDirectoryServiceImpl() {
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
    }

    /**
     * 筛选标签invoker
     *
     * @param obj RegistryDirectory
     * @param arguments 参数
     * @param result invokers
     * @return invokers
     * @see com.alibaba.dubbo.registry.integration.RegistryDirectory
     * @see org.apache.dubbo.registry.integration.RegistryDirectory
     * @see com.alibaba.dubbo.rpc.Invoker
     * @see org.apache.dubbo.rpc.Invoker
     */
    @Override
    public Object selectInvokers(Object obj, Object[] arguments, Object result) {
        if (arguments == null || arguments.length == 0) {
            return result;
        }
        Object invocation = arguments[0];
        putAttachment(invocation);
        Map<String, String> queryMap = DubboReflectUtils.getQueryMap(obj);
        if (CollectionUtils.isEmpty(queryMap)) {
            return result;
        }
        if (!CONSUMER_VALUE.equals(queryMap.get(CONSUMER_KEY))) {
            return result;
        }
        DubboCache cache = DubboCache.INSTANCE;
        String serviceInterface = queryMap.get(INTERFACE_KEY);
        String targetService = cache.getApplication(serviceInterface);
        if (StringUtils.isBlank(targetService)) {
            return result;
        }
        List<Object> list = getZoneInvokers(result, targetService);
        if (!shouldHandle(list)) {
            return list;
        }
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        if (RouterConfiguration.isInValid(configuration)) {
            return list;
        }
        String interfaceName = getGroup(queryMap) + "/" + serviceInterface + "."
            + DubboReflectUtils.getMethodName(invocation) + ":" + getVersion(queryMap);
        List<Rule> rules = RuleUtils.getRules(configuration, targetService, interfaceName, cache.getAppName());
        List<Route> routes = RouteUtils.getRoutes(rules, DubboReflectUtils.getArguments(invocation),
            DubboReflectUtils.getAttachments(invocation));
        if (!CollectionUtils.isEmpty(routes)) {
            return RuleStrategyHandler.INSTANCE.getMatchInvokers(targetService, list, routes);
        }
        return RuleStrategyHandler.INSTANCE.getMismatchInvokers(targetService, list, RuleUtils.getTags(rules));
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

    private List<Object> getZoneInvokers(Object obj, String targetService) {
        List<Object> invokers = (List<Object>) obj;
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
}