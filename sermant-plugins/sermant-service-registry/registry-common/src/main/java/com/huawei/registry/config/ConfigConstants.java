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

package com.huawei.registry.config;

/**
 * Configure constant definitions
 *
 * @author zhouss
 * @since 2022-03-02
 */
public class ConfigConstants {
    /**
     * The default instance pull interval is in seconds
     */
    public static final int DEFAULT_PULL_INTERVAL = 15;

    /**
     * Default number of failed retries for heartbeats
     */
    public static final int DEFAULT_HEARTBEAT_RETRY_TIMES = 3;

    /**
     * The default heartbeat sending interval is in seconds
     */
    public static final int DEFAULT_HEARTBEAT_INTERVAL = 15;

    /**
     * The separator between APP and Service
     */
    public static final String APP_SERVICE_SEPARATOR = ".";

    /**
     * Default value
     */
    public static final String COMMON_DEFAULT_VALUE = "default";

    /**
     * Seconds to millisecond units
     */
    public static final long SEC_DELTA = 1000L;

    /**
     * Public Registry Framework Version
     */
    public static final String COMMON_FRAMEWORK = "Sermant";

    /**
     * Whether the service encrypts public parameters
     */
    public static final String SECURE = "secure";

    private ConfigConstants() {
    }
}
