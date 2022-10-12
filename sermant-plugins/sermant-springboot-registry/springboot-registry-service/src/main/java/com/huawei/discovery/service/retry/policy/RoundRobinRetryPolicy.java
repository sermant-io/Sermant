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

package com.huawei.discovery.service.retry.policy;

import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.lb.DiscoveryManager;
import com.huawei.discovery.service.lb.rule.RoundRobinLoadbalancer;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的重试策略-轮询
 *
 * @author zhouss
 * @since 2022-09-30
 */
public class RoundRobinRetryPolicy implements RetryPolicy {
    private final Map<String, RoundRobinLoadbalancer> lbCache = new ConcurrentHashMap<>();

    @Override
    public Optional<ServiceInstance> select(String serviceName, ServiceInstance lastInstance) {
        return DiscoveryManager.INSTANCE.choose(serviceName, lbCache.computeIfAbsent(serviceName,
            name -> new RoundRobinLoadbalancer()), (serviceName1, serviceInstances) -> {
                if (serviceInstances == null || serviceInstances.size() <= 1) {
                    return serviceInstances;
                }
                serviceInstances.removeIf(instance -> instance.equals(lastInstance));
                return serviceInstances;
            });
    }
}
