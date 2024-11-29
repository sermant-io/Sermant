/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.common.utils.ReflectUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.common.xds.XdsRouterHandler;
import io.sermant.router.spring.service.LoadBalancerService;
import io.sermant.router.spring.utils.BaseHttpRouterUtils;
import io.sermant.router.spring.utils.SpringRouterUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * spring cloud loadbalancer Interception points
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class LoadBalancerInterceptor extends AbstractInterceptor {
    private final LoadBalancerService loadBalancerService;

    private final RouterConfig routerConfig;

    /**
     * Constructor
     */
    public LoadBalancerInterceptor() {
        loadBalancerService = ServiceManager.getService(LoadBalancerService.class);
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object object = context.getObject();
        String serviceId = getServiceId(object).orElse(null);
        if (StringUtils.isBlank(serviceId)) {
            return context;
        }
        Object[] arguments = context.getArguments();
        if (handleXdsRouterAndUpdateServiceInstance(serviceId, arguments)) {
            return context;
        }
        List<Object> instances = (List<Object>) arguments[0];
        if (CollectionUtils.isEmpty(instances)) {
            return context;
        }
        List<Object> targetInstances = loadBalancerService
                .getTargetInstances(serviceId, instances, ThreadLocalUtils.getRequestData());
        arguments[0] = targetInstances;
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private Optional<String> getServiceId(Object object) {
        return ReflectUtils.getFieldValue(object, "serviceId").map(obj -> (String) obj);
    }

    private boolean handleXdsRouterAndUpdateServiceInstance(String serviceName, Object[] arguments) {
        RequestData requestData = ThreadLocalUtils.getRequestData();
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
        arguments[0] = SpringRouterUtils
                .getSpringCloudServiceInstanceByXds(serviceInstanceByXdsRoute);
        return true;
    }
}