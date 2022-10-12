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
import com.huawei.discovery.service.lb.stats.ServiceStatsManager;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 最低并发负载均衡
 *
 * @author zhouss
 * @since 2022-09-29
 */
public class BestAvailableLoadbalancer extends AbstractLoadbalancer {
    @Override
    protected ServiceInstance doChoose(String serviceName, List<ServiceInstance> instances) {
        // 相同且并发数最小实例数量
        int sameActiveCount = 0;
        long minActiveRequest = -1;

        // 记录最小实例的下标
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

        // 刚好存在最小的并发实例
        if (sameActiveCount == 1) {
            return instances.get(activeIndexes[0]);
        }

        // 多个相同的并发实例采用从中随机策略选择一个实例
        return instances.get(activeIndexes[ThreadLocalRandom.current().nextInt(sameActiveCount)]);
    }

    @Override
    public String lbType() {
        return "BestAvailable";
    }
}
