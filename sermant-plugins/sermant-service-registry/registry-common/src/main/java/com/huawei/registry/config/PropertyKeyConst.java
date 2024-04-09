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

package com.huawei.registry.config;

/**
 * NACOS registers static constants for parameters
 *
 * @author chengyouling
 * @since 2022-10-25
 */
public class PropertyKeyConst {
    /**
     * Profanity
     */
    public static final String HTTP_URL_COLON = ":";

    /**
     * Node
     */
    public static final String ENDPOINT = "endpoint";

    /**
     * Node port
     */
    public static final String ENDPOINT_PORT = "endpointPort";

    /**
     * Namespace
     */
    public static final String NAMESPACE = "namespace";

    /**
     * Username
     */
    public static final String USERNAME = "username";

    /**
     * User password
     */
    public static final String PASSWORD = "password";

    /**
     * AK value
     */
    public static final String ACCESS_KEY = "accessKey";

    /**
     * SK value
     */
    public static final String SECRET_KEY = "secretKey";

    /**
     * Service address
     */
    public static final String SERVER_ADDR = "serverAddr";

    /**
     * The name of the cluster
     */
    public static final String CLUSTER_NAME = "clusterName";

    /**
     * Start naming whether to load the cache
     */
    public static final String NAMING_LOAD_CACHE_AT_START = "namingLoadCacheAtStart";

    /**
     * The name of the nacos log file
     */
    public static final String NACOS_NAMING_LOG_NAME = "com.alibaba.nacos.naming.log.filename";

    /**
     * Constructor
     */
    private PropertyKeyConst() {
    }
}
