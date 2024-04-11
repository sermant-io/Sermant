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

package com.huawei.discovery.service.lb.discovery;

import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.ex.QueryInstanceException;

import java.io.Closeable;
import java.util.Collection;

/**
 * Client
 *
 * @author zhouss
 * @since 2022-09-26
 */
public interface ServiceDiscoveryClient extends Closeable {
    /**
     * Initialize
     */
    void init();

    /**
     * Registration method
     *
     * @param serviceInstance register
     * @return true, The registration is successful
     */
    boolean registry(ServiceInstance serviceInstance);

    /**
     * Query the list of instances
     *
     * @param serviceId Service name
     * @return List of instances
     * @throws QueryInstanceException A problem is thrown if there is a query instance
     */
    Collection<ServiceInstance> getInstances(String serviceId) throws QueryInstanceException;

    /**
     * Query all service names
     *
     * @return A list of all service names
     */
    Collection<String> getServices();

    /**
     * The current instance is offline
     *
     * @return Whether the registration is successful
     */
    boolean unRegistry();

    /**
     * The name of the service discovery, associated with the registry type
     *
     * @return Name
     */
    String name();
}
