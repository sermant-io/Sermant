/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ReflectUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.service.LoadBalancerService;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.zuul.context.RequestContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

/**
 * Ribbon BaseLoadBalancer负载均衡增强类，筛选下游实例
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class BaseLoadBalancerInterceptor extends AbstractInterceptor {
    private final LoadBalancerService loadBalancerService;

    private final RouterConfig routerConfig;

    private final boolean canLoadZuul;

    /**
     * 构造方法
     */
    public BaseLoadBalancerInterceptor() {
        loadBalancerService = ServiceManager.getService(LoadBalancerService.class);
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
        canLoadZuul = canLoadZuul();
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object object = context.getObject();
        if (object instanceof BaseLoadBalancer) {
            List<Object> serverList = getServerList(context.getMethod().getName(), object);
            if (CollectionUtils.isEmpty(serverList)) {
                return context;
            }
            BaseLoadBalancer loadBalancer = (BaseLoadBalancer) object;
            String name = loadBalancer.getName();
            RequestData requestData = getRequestData().orElse(null);
            if (requestData == null) {
                return context.skip(Collections.unmodifiableList(loadBalancerService
                    .getZoneInstances(name, serverList, routerConfig.isEnabledSpringZoneRouter())));
            }
            List<Object> targetInstances = loadBalancerService
                .getTargetInstances(name, serverList, requestData.getPath(), requestData.getHeader());
            context.skip(Collections.unmodifiableList(
                loadBalancerService.getZoneInstances(name, targetInstances, routerConfig.isEnabledSpringZoneRouter())));
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private List<Object> getServerList(String methodName, Object obj) {
        String fieldName = "getAllServers".equals(methodName) ? "allServerList" : "upServerList";
        return ReflectUtils.getFieldValue(obj, fieldName).map(value -> (List<Object>) value)
            .orElse(Collections.emptyList());
    }

    private Optional<RequestData> getRequestData() {
        RequestData requestData = ThreadLocalUtils.getRequestData();
        if (requestData != null) {
            return Optional.of(requestData);
        }
        if (!canLoadZuul) {
            return Optional.empty();
        }
        RequestContext context = RequestContext.getCurrentContext();
        if (context == null || context.getRequest() == null) {
            return Optional.empty();
        }
        Map<String, List<String>> header = new HashMap<>();
        HttpServletRequest request = context.getRequest();
        Enumeration<?> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = (String) headerNames.nextElement();
            header.put(name, enumeration2List(request.getHeaders(name)));
        }
        return Optional.of(new RequestData(header, (String) context.get("requestURI"), request.getMethod()));
    }

    private List<String> enumeration2List(Enumeration<?> enumeration) {
        if (enumeration == null) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            list.add((String) enumeration.nextElement());
        }
        return list;
    }

    private boolean canLoadZuul() {
        try {
            Class.forName(RequestContext.class.getCanonicalName());
        } catch (NoClassDefFoundError | ClassNotFoundException error) {
            return false;
        }
        return true;
    }
}