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
import com.huawei.discovery.service.lb.utils.CommonUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 负载均衡测试
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class RoundRobinLoadbalancerTest extends BaseLoadbalancerTest {
    @Test
    public void doChoose() {
        int port1 = 9999;
        int port2 = 8888;
        String serviceName = "round";
        final List<ServiceInstance> serviceInstances = Arrays
                .asList(CommonUtils.buildInstance(serviceName, port1), CommonUtils.buildInstance(serviceName, port2));
        final RoundRobinLoadbalancer roundRobinLoadbalancer = new RoundRobinLoadbalancer();
        // 模拟调用两次, 每个实例选择一次
        int count = 2;
        for (int i = 0; i < count; i++) {
            final Optional<ServiceInstance> choose = roundRobinLoadbalancer.choose(serviceName, serviceInstances);
            Assert.assertTrue(choose.isPresent());
            if (port1 == choose.get().getPort()) {
                port1--;
            }
            if (port2 == choose.get().getPort()) {
                port2--;
            }
        }
        Assert.assertTrue(port1 == 9998 && port2 == 8887);
    }

    @Override
    protected AbstractLoadbalancer getLb() {
        return new RoundRobinLoadbalancer();
    }

    @Override
    protected String lb() {
        return "RoundRobin";
    }
}
