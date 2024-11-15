/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.router.common.xds.lb;

import io.sermant.core.service.xds.entity.ServiceInstance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * XdsRoundRobinLoadBalancer
 *
 * @author daizhenyu
 * @since 2024-08-30
 **/
public class XdsRoundRobinLoadBalancer implements XdsLoadBalancer {
    private final AtomicInteger index;

    /**
     * constructor
     */
    public XdsRoundRobinLoadBalancer() {
        this.index = new AtomicInteger(0);
    }

    @Override
    public ServiceInstance selectInstance(List<ServiceInstance> instances) {
        synchronized (XdsRoundRobinLoadBalancer.class) {
            // safely calculate the index based on the current size of the instances list
            int currentIndex = index.getAndUpdate(i -> (i + 1) % instances.size());

            // double-check size to avoid index out of bounds
            if (currentIndex >= instances.size()) {
                currentIndex = 0;
                index.set(1);
            }

            // return the instance at the current index
            return instances.get(currentIndex);
        }
    }
}
