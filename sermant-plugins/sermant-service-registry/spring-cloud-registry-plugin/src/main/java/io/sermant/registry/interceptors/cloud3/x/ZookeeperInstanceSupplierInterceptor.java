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

package io.sermant.registry.interceptors.cloud3.x;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.registry.context.RegisterContext;
import io.sermant.registry.support.InstanceInterceptorSupport;
import io.sermant.registry.utils.HostUtils;

import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Obtain interception for ZooKeeper 3.x auto-discovery instances
 *
 * @author zhouss
 * @since 2022-03-29
 */
public class ZookeeperInstanceSupplierInterceptor extends InstanceInterceptorSupport {
    private static final String SERVICE_INSTANCE_CLASS_NAME = "io.sermant.registry.entity.DiscoveryServiceInstance";

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        context.changeResult(getMergedInstances(context));
        return context;
    }

    private List<ServiceInstance> getMergedInstances(ExecuteContext context) {
        // Filtered service instances
        final Object originServiceInstances = context.getResult();

        // Full service instances
        final Object allServiceInstances = context.getArguments()[0];
        final List<ServiceInstance> serviceInstances = filterDiscoveryServiceInstance(
                (List<ServiceInstance>) allServiceInstances);
        if (originServiceInstances instanceof List) {
            return convertAndMerge(serviceInstances, (List<ServiceInstance>) originServiceInstances);
        }
        return convertAndMerge(serviceInstances, Collections.emptyList());
    }

    /**
     * Since the query instance method has been called before
     * {@link org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClient} is called before the method
     * is called， Therefore, to avoid re-querying, the list of queried instances is filtered out
     *
     * @param serviceInstances List of instances
     * @return Filtered instances
     */
    private List<ServiceInstance> filterDiscoveryServiceInstance(List<ServiceInstance> serviceInstances) {
        return serviceInstances.stream()
                .filter(serviceInstance -> SERVICE_INSTANCE_CLASS_NAME.equals(serviceInstance.getClass().getName()))
                .collect(Collectors.toList());
    }

    private List<ServiceInstance> convertAndMerge(List<ServiceInstance> microServiceInstances,
            List<ServiceInstance> originServiceInstances) {
        List<ServiceInstance> result = new ArrayList<>();
        if (isOpenMigration() && RegisterContext.INSTANCE.isAvailable()) {
            result.addAll(originServiceInstances);
        }
        for (ServiceInstance microServiceInstance : microServiceInstances) {
            result.removeIf(originServiceInstance ->
                    HostUtils.isSameInstance(originServiceInstance.getHost(), originServiceInstance.getPort(),
                            microServiceInstance.getHost(), microServiceInstance.getPort()));
            result.add(microServiceInstance);
        }
        return result;
    }

    /**
     * Obtain the instance class associated with the host
     *
     * @return The class permission is named
     */
    @Override
    protected String getInstanceClassName() {
        return SERVICE_INSTANCE_CLASS_NAME;
    }
}
