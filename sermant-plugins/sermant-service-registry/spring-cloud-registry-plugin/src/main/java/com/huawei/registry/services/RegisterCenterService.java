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

package com.huawei.registry.services;

import com.huawei.registry.entity.FixedResult;
import com.huawei.registry.entity.MicroServiceInstance;

import com.huaweicloud.sermant.core.plugin.service.PluginService;

import java.util.List;

/**
 * Register for the service class
 *
 * @author zhouss
 * @since 2021-12-16
 */
public interface RegisterCenterService extends PluginService {
    /**
     * Intercept the registration method of the original spring
     *
     * @param result Pre-return
     */
    void register(FixedResult result);

    /**
     * Stop method
     */
    void unRegister();

    /**
     * Obtain the list of instances
     *
     * @param serviceId Service name
     * @return List of instances
     */
    List<MicroServiceInstance> getServerList(String serviceId);

    /**
     * Get a list of service names
     *
     * @return List of services
     */
    List<String> getServices();

    /**
     * Get registry status
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
