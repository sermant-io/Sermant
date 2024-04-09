/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huawei.registry.service.register;

import com.huawei.registry.config.RegisterType;
import com.huawei.registry.entity.MicroServiceInstance;

import java.util.List;

/**
 * Register
 *
 * @author zhouss
 * @since 2021-12-17
 */
public interface Register {
    /**
     * Service startup status
     */
    String UP = "UP";

    /**
     * Service shutdown status
     */
    String DOWN = "DOWN";

    /**
     * Unknown status
     */
    String UN_KNOWN = "UN_KNOWN";

    /**
     * Registration initialization
     */
    void start();

    /**
     * Stop method
     */
    void stop();

    /**
     * Intercept the registration method of the original spring
     */
    void register();

    /**
     * Replace the service list based on DiscoveryClient interception
     *
     * @param <T> Instance information
     * @param serviceId Service ID
     * @return List of services
     */
    <T extends MicroServiceInstance> List<T> getInstanceList(String serviceId);

    /**
     * Get a list of service names
     *
     * @return List of service names
     */
    List<String> getServices();

    /**
     * Registry type
     *
     * @return register type
     */
    RegisterType registerType();

    /**
     * Get the status of the current registry
     *
     * @return UP DOWN
     */
    String getRegisterCenterStatus();

    /**
     * Obtain the status of the current instance
     *
     * @return Instance status
     */
    String getInstanceStatus();

    /**
     * Update the instance status
     *
     * @param status Target status
     */
    void updateInstanceStatus(String status);
}
