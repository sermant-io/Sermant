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

package com.huawei.discovery.entity;

import java.util.Map;

/**
 * Instance
 *
 * @author zhouss
 * @since 2022-09-26
 */
public interface ServiceInstance {
    /**
     * Unique identifiers ip:port
     *
     * @return id
     */
    String getId();

    /**
     * The name of the service to which it belongs
     *
     * @return Service name
     */
    String getServiceName();

    /**
     * Get a domain name
     *
     * @return domain name
     */
    String getHost();

    /**
     * Get an IP address
     *
     * @return IP
     */
    String getIp();

    /**
     * Port
     *
     * @return port
     */
    int getPort();

    /**
     * Get the source data
     *
     * @return metadata
     */
    Map<String, String> getMetadata();

    /**
     * State
     *
     * @return Service status
     */
    String status();

    /**
     * Determine whether it is equal to the goal
     *
     * @param target Target Audience
     * @return Whether it is equal
     */
    @Override
    boolean equals(Object target);

    /**
     * Rewrite the hashcode method
     *
     * @return hash code
     */
    @Override
    int hashCode();

    /**
     * Service instance status
     *
     * @since 2022-09-28
     */
    enum Status {
        /**
         * Available
         */
        UP,

        /**
         * not-available
         */
        DOWN,

        /**
         * Unknown
         */
        UN_KNOW;
    }
}
