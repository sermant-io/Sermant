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

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.utils.StringUtils;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.utils.CollectionUtils;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.common.xds.XdsRouterHandler;
import io.sermant.router.spring.service.LoadBalancerService;
import io.sermant.router.spring.utils.BaseHttpRouterUtils;
import io.sermant.router.spring.utils.SpringRouterUtils;
import reactor.core.publisher.Flux;

import org.springframework.cloud.loadbalancer.core.DiscoveryClientServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * CachingServiceInstance List Supplier/DiscoveryClientServiceInstance List Supplier enhanced class, filtering
 * downstream instances
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class ServiceInstanceListSupplierInterceptor extends AbstractInterceptor {
    private final LoadBalancerService loadBalancerService;

    private final RouterConfig routerConfig;

    /**
     * Constructor
     */
    public ServiceInstanceListSupplierInterceptor() {
        loadBalancerService = PluginServiceManager.getPluginService(LoadBalancerService.class);
        routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);
    }

    @Override
    public ExecuteContext before(ExecuteContext context) {
        Object object = context.getObject();
        String serviceId = getServiceId(object).orElse(null);
        if (StringUtils.isBlank(serviceId)) {
            return context;
        }
        if (handleXdsRouterAndUpdateServiceInstance(serviceId, context)) {
            return context;
        }
        Object obj = context.getMemberFieldValue("serviceInstances");
        if (obj instanceof Flux<?>) {
            List<Object> instances = getInstances((Flux<Object>) obj, object);
            if (CollectionUtils.isEmpty(instances)) {
                return context;
            }
            List<Object> targetInstances = loadBalancerService
                    .getTargetInstances(serviceId, instances, ThreadLocalUtils.getRequestData());
            context.skip(Flux.just(targetInstances));
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    private Optional<String> getServiceId(Object object) {
        if (object instanceof ServiceInstanceListSupplier) {
            return Optional.ofNullable(((ServiceInstanceListSupplier) object).getServiceId());
        }
        return Optional.empty();
    }

    private List<Object> getInstances(Flux<Object> flux, Object object) {
        if (object instanceof DiscoveryClientServiceInstanceListSupplier) {
            if (flux.getClass().getName().contains("FluxFirstNonEmptyEmitting")) {
                return flux.collectList().toProcessor().block();
            }

            // This case is not handled, so an empty list is returned
            return Collections.emptyList();
        }
        return (List<Object>) flux.next().toProcessor().block();
    }

    private boolean handleXdsRouterAndUpdateServiceInstance(String serviceName, ExecuteContext context) {
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
        context.skip(Flux.just(SpringRouterUtils
                .getSpringCloudServiceInstanceByXds(serviceInstanceByXdsRoute)));
        return true;
    }
}
