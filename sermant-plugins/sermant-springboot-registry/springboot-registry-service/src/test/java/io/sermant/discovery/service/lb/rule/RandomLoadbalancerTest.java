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

package io.sermant.discovery.service.lb.rule;

import io.sermant.discovery.entity.ServiceInstance;
import io.sermant.discovery.service.lb.utils.CommonUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Random load balancing test
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class RandomLoadbalancerTest extends BaseLoadbalancerTest {
    @Test
    public void doChoose() {
        final RandomLoadbalancer randomLoadbalancer = new RandomLoadbalancer();
        int port1 = 9999;
        int port2 = 8888;
        String serviceName = "random";
        final List<ServiceInstance> serviceInstances = Arrays
                .asList(CommonUtils.buildInstance(serviceName, port1), CommonUtils.buildInstance(serviceName, port2));
        int lastPort = 0;
        int curPort;
        boolean isOver = false;
        for (int i = 0; i < 500; i++) {
            // Simulate whether there will be a contiguous port, and if it exists, it will take effect randomly
            final Optional<ServiceInstance> choose = randomLoadbalancer.choose(serviceName, serviceInstances);
            Assert.assertTrue(choose.isPresent());
            curPort = choose.get().getPort();
            if (curPort == lastPort) {
                isOver = true;
                break;
            }
            lastPort = curPort;
        }
        Assert.assertTrue(isOver);
    }

    @Test
    public void lbType() {
        Assert.assertEquals(new RandomLoadbalancer().lbType(), "Random");
    }

    @Override
    protected AbstractLoadbalancer getLb() {
        return new RandomLoadbalancer();
    }

    @Override
    protected String lb() {
        return "Random";
    }
}
