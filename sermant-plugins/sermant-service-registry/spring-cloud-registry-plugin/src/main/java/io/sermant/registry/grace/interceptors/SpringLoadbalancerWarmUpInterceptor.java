/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/dubbo/rpc/cluster/loadbalance/ShortestResponseLoadBalance.java
 * from the Apache Dubbo project.
 */

package io.sermant.registry.grace.interceptors;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.registry.config.grace.GraceContext;
import io.sermant.registry.config.grace.GraceShutDownManager;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Spring selects an instance based on the warm-up parameters
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class SpringLoadbalancerWarmUpInterceptor extends GraceSwitchInterceptor {
    private static final String RESPONSE_REACTIVE_CLASS =
            "org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse";

    private static final String RESPONSE_CLASS =
            "org.springframework.cloud.client.loadbalancer.DefaultResponse";

    @Override
    protected ExecuteContext doBefore(ExecuteContext context) {
        final Object rawServiceInstances = context.getArguments()[0];
        context.getArguments()[0] = filterOfflineInstance((List<ServiceInstance>) rawServiceInstances);
        return context;
    }

    @Override
    protected ExecuteContext doAfter(ExecuteContext context) {
        final Object result = context.getResult();
        final Object rawServiceInstances = context.getArguments()[0];
        if (!isTargetResponse(result) || !(rawServiceInstances instanceof List)) {
            return context;
        }
        List<ServiceInstance> serviceInstances = (List<ServiceInstance>) rawServiceInstances;
        if (serviceInstances.size() <= 1 || !graceConfig.isEnableWarmUp()) {
            return context;
        }
        boolean isAllWarmed = true;
        int[] weights = new int[serviceInstances.size()];
        int totalWeight = 0;
        int index = 0;
        for (ServiceInstance serviceInstance : serviceInstances) {
            final Map<String, String> metadata = serviceInstance.getMetadata();
            final boolean isWarmed = calculate(metadata, weights, index);
            isAllWarmed &= isWarmed;
            if (!isWarmed) {
                warmMessage(serviceInstance.getHost(), serviceInstance.getPort());
            }
            totalWeight += weights[index++];
        }
        if (!isAllWarmed) {
            final Optional<Object> chooseResult = chooseServer(totalWeight, weights, serviceInstances);
            chooseResult.ifPresent(serviceInstance -> {
                ReflectUtils.setFieldValue(result, "serviceInstance", serviceInstance);
            });
        }
        return context;
    }

    /**
     * Instances that have been notified to go offline are directly removed
     *
     * @param serviceInstances List of original instances
     * @return Filtered services
     */
    private List<ServiceInstance> filterOfflineInstance(List<ServiceInstance> serviceInstances) {
        if (graceConfig.isEnableGraceShutdown()) {
            final GraceShutDownManager graceShutDownManager = GraceContext.INSTANCE.getGraceShutDownManager();
            return serviceInstances.stream()
                    .filter(serviceInstance -> !graceShutDownManager.isMarkedOffline(
                            buildEndpoint(serviceInstance.getHost(), serviceInstance.getPort())))
                    .collect(Collectors.toList());
        }
        return serviceInstances;
    }

    private boolean isTargetResponse(Object result) {
        return StringUtils.equals(result.getClass().getName(), RESPONSE_CLASS)
                || isReactiveResponse(result);
    }

    private boolean isReactiveResponse(Object result) {
        return StringUtils.equals(result.getClass().getName(), RESPONSE_REACTIVE_CLASS);
    }
}
