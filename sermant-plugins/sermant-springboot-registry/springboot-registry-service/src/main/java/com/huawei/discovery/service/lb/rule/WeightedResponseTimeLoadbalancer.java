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

package com.huawei.discovery.service.lb.rule;

import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.lb.stats.InstanceStats;
import com.huawei.discovery.service.lb.stats.ServiceStats;
import com.huawei.discovery.service.lb.stats.ServiceStatsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Load balancing based on response time
 *
 * @author zhouss
 * @since 2022-09-29
 */
public class WeightedResponseTimeLoadbalancer extends AbstractLoadbalancer {
    private static final double DOUBLE_GAP = 1e-6d;

    private final AbstractLoadbalancer defaultLb = new RoundRobinLoadbalancer();

    @Override
    protected ServiceInstance doChoose(String serviceName, List<ServiceInstance> instances) {
        final ServiceStats serviceStats = ServiceStatsManager.INSTANCE.getServiceStats(serviceName);
        List<Double> responseTimeWeights = calculateResponseTimeWeight(serviceStats, instances);
        double maxWeights = responseTimeWeights.get(responseTimeWeights.size() - 1);
        if (maxWeights <= DOUBLE_GAP) {
            // At this time, the response time has not yet been counted, and a round-robin call is used
            return defaultLb.doChoose(serviceName, instances);
        }
        final double seed = ThreadLocalRandom.current().nextDouble(maxWeights);
        ServiceInstance result = null;
        for (int i = 0, size = responseTimeWeights.size(); i < size; i++) {
            if (seed <= responseTimeWeights.get(i)) {
                result = instances.get(i);
                break;
            }
        }
        if (result == null) {
            return defaultLb.doChoose(serviceName, instances);
        }
        return result;
    }

    /**
     * Respond time weights are calculated
     *
     * @param serviceInstances Service instances
     * @param serviceStats Service Metrics Information
     * @return List of weights
     */
    private List<Double> calculateResponseTimeWeight(ServiceStats serviceStats,
            List<ServiceInstance> serviceInstances) {
        final List<InstanceStats> instanceStats = serviceInstances.stream().map(serviceStats::getStats)
                .collect(Collectors.toList());
        double total = 0d;
        for (InstanceStats stats : instanceStats) {
            total += stats.getResponseAvgTime();
        }
        double indexWeight = 0d;
        final List<Double> weights = new ArrayList<>(instanceStats.size());
        for (InstanceStats stats : instanceStats) {
            final double curWeight = total - stats.getResponseAvgTime();
            indexWeight += curWeight;
            weights.add(indexWeight);
        }
        return weights;
    }

    @Override
    public String lbType() {
        return "WeightedResponseTime";
    }
}
