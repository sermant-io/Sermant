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

import com.huawei.discovery.entity.DefaultServiceInstance;
import com.huawei.discovery.entity.ServiceInstance;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 抽象负载均衡测试
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class AbstractLoadbalancerTest {
    @Test
    public void choose() {
        final AbstractLoadbalancer roundRobinLoadbalancer = new RoundRobinLoadbalancer();
        final Optional<ServiceInstance> choose = roundRobinLoadbalancer.choose(null, null);
        Assert.assertFalse(choose.isPresent());

        // 测试一个实例
        final List<ServiceInstance> serviceInstances = Collections.singletonList(build());
        final Optional<ServiceInstance> test = roundRobinLoadbalancer.choose("test", serviceInstances);
        Assert.assertTrue(test.isPresent());
        Assert.assertEquals(test.get(), serviceInstances.get(0));

        // 测试多个实例
        final List<ServiceInstance> serviceInstances1 = Arrays.asList(build(), build());
        final Optional<ServiceInstance> instance = roundRobinLoadbalancer.choose("name", serviceInstances1);
        Assert.assertTrue(instance.isPresent());
        Assert.assertTrue(serviceInstances1.contains(instance.get()));
    }

    private ServiceInstance build() {
        return new DefaultServiceInstance("localhost", "127.0.0.1", 8080,
                Collections.emptyMap(), "zk");
    }
}
