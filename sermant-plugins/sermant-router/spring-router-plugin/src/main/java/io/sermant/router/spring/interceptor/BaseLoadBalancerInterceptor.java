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

package io.sermant.router.spring.interceptor;

import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.zuul.context.RequestContext;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.request.RequestTag;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.common.utils.ReflectUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.common.xds.XdsRouterHandler;
import io.sermant.router.spring.service.LoadBalancerService;
import io.sermant.router.spring.utils.BaseHttpRouterUtils;
import io.sermant.router.spring.utils.SpringRouterUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * The Ribbon BaseLoadBalancer enhanced load balancer class filters downstream instances
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class BaseLoadBalancerInterceptor extends AbstractInterceptor {
    private final LoadBalancerService loadBalancerService;

    private final boolean canLoadZuul;

    private final RouterConfig routerConfig;

    /**
     * Constructor
     */
    public BaseLoadBalancerInterceptor() {
        loadBalancerService = PluginServiceManager.getPluginService(LoadBalancerService.class);
        canLoadZuul = canLoadZuul();
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object object = context.getObject();
        if (!(object instanceof BaseLoadBalancer)) {
            return context;
        }
        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) object;
        String name = loadBalancer.getName();
        RequestData requestData = getRequestData().orElse(null);

        if (handleXdsRouterAndUpdateServiceInstance(name, requestData, context)) {
            return context;
        }
        List<Object> serverList = getServerList(context.getMethod().getName(), object);
        if (CollectionUtils.isEmpty(serverList)) {
            return context;
        }

        List<Object> targetInstances = loadBalancerService.getTargetInstances(name, serverList, requestData);
        context.skip(Collections.unmodifiableList(targetInstances));
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
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        if (requestTag != null) {
            header.putAll(requestTag.getTag());
        }
        HttpServletRequest request = context.getRequest();
        String scheme = request.getScheme();
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

    private boolean handleXdsRouterAndUpdateServiceInstance(String serviceName, RequestData requestData,
            ExecuteContext context) {
        if (requestData == null || (!routerConfig.isEnabledXdsRoute())) {
            return false;
        }

        // use xds route to find service instances
        Set<ServiceInstance> serviceInstanceByXdsRoute = XdsRouterHandler.INSTANCE
                .getServiceInstanceByXdsRoute(serviceName, requestData.getPath(),
                        BaseHttpRouterUtils.processHeaders(requestData.getTag()));
        if (CollectionUtils.isEmpty(serviceInstanceByXdsRoute)) {
            return false;
        }
        context.skip(Collections.unmodifiableList(SpringRouterUtils
                .getSpringCloudServerByXds(serviceInstanceByXdsRoute)));
        return true;
    }
}