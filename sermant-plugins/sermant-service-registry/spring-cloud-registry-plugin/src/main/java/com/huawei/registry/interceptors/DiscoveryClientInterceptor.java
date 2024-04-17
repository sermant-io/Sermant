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

package com.huawei.registry.interceptors;

import com.huawei.registry.config.RegisterDynamicConfig;
import com.huawei.registry.context.RegisterContext;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.services.RegisterCenterService;
import com.huawei.registry.support.InstanceInterceptorSupport;
import com.huawei.registry.utils.HostUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;

import reactor.core.publisher.Flux;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Intercept to get a list of services
 *
 * @author zhouss
 * @since 2021-12-13
 */
public class DiscoveryClientInterceptor extends InstanceInterceptorSupport {
    private static final String SERVICE_ID = "serviceId";

    private static final String MICRO_SERVICE_INSTANCES = "microServiceInstances";

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        String serviceId = (String) context.getArguments()[0];
        final RegisterCenterService service = PluginServiceManager.getPluginService(RegisterCenterService.class);
        final List<MicroServiceInstance> microServiceInstances = service.getServerList(serviceId);
        if (microServiceInstances.isEmpty()) {
            return context;
        }
        context.setLocalFieldValue(SERVICE_ID, serviceId);
        context.setLocalFieldValue(MICRO_SERVICE_INSTANCES, microServiceInstances);
        if (RegisterContext.INSTANCE.isAvailable()
                && !RegisterDynamicConfig.INSTANCE.isNeedCloseOriginRegisterCenter()) {
            return context;
        }
        final Object target = context.getObject();
        context.skip(isWebfLux(target) ? Flux.fromIterable(Collections.emptyList())
                : Collections.emptyList());
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        final String serviceId = (String) context.getLocalFieldValue(SERVICE_ID);
        final List<MicroServiceInstance> microServiceInstances =
                (List<MicroServiceInstance>) context.getLocalFieldValue(MICRO_SERVICE_INSTANCES);
        if (microServiceInstances != null && !microServiceInstances.isEmpty()) {
            final Object target = context.getObject();
            final Object contextResult = context.getResult();
            context.changeResult(
                    isWebfLux(target) ? convertAndMergeWithFlux(microServiceInstances, serviceId, target, contextResult)
                            : convertAndMerge(microServiceInstances, serviceId, target, contextResult));
        }
        return context;
    }

    private Flux<ServiceInstance> convertAndMergeWithFlux(
            List<MicroServiceInstance> microServiceInstances,
            String serviceId, Object target, Object contextResult) {
        return Flux.fromIterable(convertAndMerge(microServiceInstances, serviceId, target, contextResult));
    }

    private List<ServiceInstance> convertAndMerge(List<MicroServiceInstance> microServiceInstances,
            String serviceId,
            Object target, Object contextResult) {
        List<ServiceInstance> result = new ArrayList<>(microServiceInstances.size());
        result.addAll(queryOriginInstances(target, contextResult));
        for (MicroServiceInstance microServiceInstance : microServiceInstances) {
            result.removeIf(originServiceInstance ->
                    HostUtils.isSameInstance(originServiceInstance.getHost(), originServiceInstance.getPort(),
                            microServiceInstance.getHost(), microServiceInstance.getPort()));
            buildInstance(microServiceInstance, serviceId)
                    .ifPresent(instance -> result.add((ServiceInstance) instance));
        }
        return result.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<ServiceInstance> queryOriginInstances(Object target, Object contextResult) {
        if (target instanceof CompositeDiscoveryClient) {
            return contextResult == null ? Collections.emptyList() : (List<ServiceInstance>) contextResult;
        }
        if (isWebfLux(target)) {
            List<ServiceInstance> resultList = new ArrayList<>();
            final Flux<ServiceInstance> instances = (Flux<ServiceInstance>) contextResult;
            instances.collectList().subscribe(resultList::addAll);
            return resultList;
        }
        return Collections.emptyList();
    }

    /**
     * Obtain the instance class associated with the host
     *
     * @return The class permission is named
     */
    @Override
    protected String getInstanceClassName() {
        return "com.huawei.registry.entity.DiscoveryServiceInstance";
    }
}
