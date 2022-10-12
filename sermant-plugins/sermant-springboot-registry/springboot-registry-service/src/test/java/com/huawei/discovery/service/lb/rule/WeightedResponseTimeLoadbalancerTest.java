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
import com.huawei.discovery.service.lb.utils.CommonUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 响应时间
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class WeightedResponseTimeLoadbalancerTest extends BaseLoadbalancerTest {
    @Test
    public void doChoose() {
        final WeightedResponseTimeLoadbalancer weightedResponseTimeLoadbalancer =
                new WeightedResponseTimeLoadbalancer();
        int port1 = 9999;
        int port2 = 8888;
        String serviceName = "lb";
        final ServiceInstance serviceInstance1 = CommonUtils.buildInstance(serviceName, port1);
        final ServiceInstance serviceInstance2 = CommonUtils.buildInstance(serviceName, port2);
        final ServiceStats serviceStats = ServiceStatsManager.INSTANCE.getServiceStats(serviceName);
        final InstanceStats stats1 = serviceStats.getStats(serviceInstance1);
        for (int i =0; i < 10; i ++) {
            // 模拟第一个实例响应时间很长
            stats1.beforeRequest();
            stats1.afterRequest(1000L);
        }
        final List<ServiceInstance> serviceInstances = Arrays.asList(serviceInstance1, serviceInstance2);
        // 模拟100次请求, 并发度低的实例将更容易被选中
        int count1 = 0, count2 = 0;
        for (int i = 0; i < 100; i++) {
            final Optional<ServiceInstance> choose = weightedResponseTimeLoadbalancer
                    .choose(serviceName, serviceInstances);
            Assert.assertTrue(choose.isPresent());
            if (choose.get().getPort() == port1) {
                count1++;
            } else {
                count2++;
            }
        }
        Assert.assertTrue(count1 < count2);
    }

    @Override
    protected AbstractLoadbalancer getLb() {
        return new WeightedResponseTimeLoadbalancer();
    }

    @Override
    protected String lb() {
        return "WeightedResponseTime";
    }
}
