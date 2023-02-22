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
import com.huaweicloud.sermant.core.utils.StringUtils;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.common.utils.CollectionUtils;
import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;
import com.huaweicloud.sermant.router.spring.service.LoadBalancerService;

import reactor.core.publisher.Flux;

import org.springframework.cloud.loadbalancer.core.DiscoveryClientServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * CachingServiceInstanceListSupplier/DiscoveryClientServiceInstanceListSupplier增强类，筛选下游实例
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class ServiceInstanceListSupplierInterceptor extends AbstractInterceptor {
    private final LoadBalancerService loadBalancerService;

    private final RouterConfig routerConfig;

    /**
     * 构造方法
     */
    public ServiceInstanceListSupplierInterceptor() {
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
        Object obj = context.getMemberFieldValue("serviceInstances");
        if (obj instanceof Flux<?>) {
            List<Object> instances = getInstances((Flux<Object>) obj, object);
            if (CollectionUtils.isEmpty(instances)) {
                return context;
            }
            RequestData requestData = ThreadLocalUtils.getRequestData();
            List<Object> targetInstances = loadBalancerService
                    .getTargetInstances(serviceId, instances, requestData);
            context.skip(Flux.just(Collections.unmodifiableList(targetInstances)));
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

            // 这种情况不处理，所以返回emptyList
            return Collections.emptyList();
        }
        return (List<Object>) flux.next().block();
    }
}