/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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
import com.huawei.discovery.service.lb.stats.ServiceStatsManager;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Minimum concurrent load balancing
 *
 * @author zhouss
 * @since 2022-09-29
 */
public class BestAvailableLoadbalancer extends AbstractLoadbalancer {
    @Override
    protected ServiceInstance doChoose(String serviceName, List<ServiceInstance> instances) {
        // The minimum number of instances with the same number of concurrent instances
        int sameActiveCount = 0;
        long minActiveRequest = -1;

        // Record the subscript for the least instance
        final int[] activeIndexes = new int[instances.size()];
        for (int index = 0, size = instances.size(); index < size; index++) {
            final ServiceInstance serviceInstance = instances.get(index);
            final InstanceStats instanceStats = ServiceStatsManager.INSTANCE.getInstanceStats(serviceInstance);
            final long activeRequest = instanceStats.getActiveRequests();
            if (minActiveRequest == -1 || activeRequest < minActiveRequest) {
                minActiveRequest = activeRequest;
                sameActiveCount = 1;
                activeIndexes[0] = index;
            } else if (minActiveRequest == activeRequest) {
                activeIndexes[sameActiveCount++] = index;
            }
        }

        // There is just the least concurrent instance
        if (sameActiveCount == 1) {
            return instances.get(activeIndexes[0]);
        }

        // Multiple identical concurrent instances are selected from a random strategy
        return instances.get(activeIndexes[ThreadLocalRandom.current().nextInt(sameActiveCount)]);
    }

    @Override
    public String lbType() {
        return "BestAvailable";
    }
}
