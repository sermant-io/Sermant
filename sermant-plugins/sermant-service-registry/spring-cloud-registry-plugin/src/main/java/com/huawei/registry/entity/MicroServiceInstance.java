/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.entity;

import java.util.Map;

/**
 * Define instance information
 *
 * @author zhouss
 * @since 2022-02-17
 */
public interface MicroServiceInstance {
    /**
     * Service name
     *
     * @return Service name
     */
    String getServiceName();

    /**
     * domain name
     *
     * @return host
     */
    String getHost();

    /**
     * The IP address of the current instance
     *
     * @return ip
     */
    String getIp();

    /**
     * port
     *
     * @return Port
     */
    int getPort();

    /**
     * Service ID
     *
     * @return Service ID
     */
    String getServiceId();

    /**
     * Instance ID
     *
     * @return Instance ID
     */
    String getInstanceId();

    /**
     * Obtain the metadata information of the instance
     *
     * @return Metadata
     */
    Map<String, String> getMetadata();

    /**
     * Whether it is encrypted or not
     *
     * @return Whether it is encrypted or not
     */
    boolean isSecure();
}
