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

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡
 *
 * @author zhouss
 * @since 2022-09-26
 */
public class RoundRobinLoadbalancer extends AbstractLoadbalancer {
    private static final int SEED = 999;

    private final AtomicInteger position;

    /**
     * 构造器
     */
    public RoundRobinLoadbalancer() {
        position = new AtomicInteger(new Random().nextInt(SEED));
    }

    @Override
    public ServiceInstance doChoose(String serviceName, List<ServiceInstance> instances) {
        final int index = Math.abs(position.incrementAndGet());
        return instances.get(index % instances.size());
    }

    @Override
    public String lbType() {
        return "RoundRobin";
    }
}
