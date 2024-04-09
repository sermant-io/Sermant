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

import java.util.List;
import java.util.Optional;

/**
 * Load balancing
 *
 * @author zhouss
 * @since 2022-09-26
 */
public interface Loadbalancer {
    /**
     * Select an instance
     *
     * @param serviceName Service name
     * @param instances List of instances
     * @return ServiceInstance
     */
    Optional<ServiceInstance> choose(String serviceName, List<ServiceInstance> instances);
}
