/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.registry.config.grace;

/**
 * Elegant online and offline public variables
 *
 * @author zhouss
 * @since 2022-05-17
 */
public class GraceConstants {
    /**
     * Spring LoadBalancer provides the name of the instance cache generator, based on which the specified service
     * instance is flushed to the local cache
     */
    public static final String SPRING_CACHE_MANAGER_LOADBALANCER_CACHE_NAME = "CachingServiceInstanceListSupplierCache";

    /**
     * Warm-up time-key
     */
    public static final String WARM_KEY_TIME = "sermant.grace.warmup.time";

    /**
     * Pre-Warm Injection Time-key
     */
    public static final String WARM_KEY_INJECT_TIME = "sermant.grace.warmup.inject.time";

    /**
     * Preheat Weight - Key
     */
    public static final String WARM_KEY_WEIGHT = "sermant.grace.warmup.weight";

    /**
     * The warm-up type - used to calculate the traffic distribution - key
     */
    public static final String WARM_KEY_CURVE = "sermant.grace.warmup.cal.curve";

    /**
     * The name of the service to be shut down is used to return the response to the upstream
     */
    public static final String MARK_SHUTDOWN_SERVICE_NAME = "sermant.grace.mark.shutdown.service.name";

    /**
     * The endpoint that is marked for closure is used to return the response to the upstream
     */
    public static final String MARK_SHUTDOWN_SERVICE_ENDPOINT = "sermant.grace.mark.shutdown.service.endpoint";

    /**
     * The downstream sends offline notifications based on the address
     */
    public static final String SERMANT_GRACE_ADDRESS = "sermant.grace.address";

    /**
     * Graceful Offline Aggregation Switch Environment Variable, set to true to enable all functions of Graceful Offline
     * and Convergence
     */
    public static final String ENV_GRACE_ENABLE = "grace.rule.enableGrace";

    /**
     * Default notification HTTP port
     */
    public static final int DEFAULT_NOTIFY_HTTP_SERVER_PORT = 16688;

    /**
     * Default warm-up weight
     */
    public static final int DEFAULT_WARM_UP_WEIGHT = 100;

    /**
     * The default traffic allocation calculation curve value
     */
    public static final int DEFAULT_WARM_UP_CURVE = 2;

    /**
     * The default injection time is 1 hour
     */
    public static final String DEFAULT_WARM_UP_INJECT_TIME_GAP = "0";

    /**
     * Default warm-up time
     */
    public static final String DEFAULT_WARM_UP_TIME = "0";

    /**
     * The default downstream endpoint expiration time is 120S
     */
    public static final long DEFAULT_ENDPOINT_EXPIRED_TIME = 120L;

    /**
     * Proactively notify the URL path
     */
    public static final String GRACE_NOTIFY_URL_PATH = "/$$sermant$$/notify";

    /**
     * The URL path of the downline
     */
    public static final String GRACE_SHUTDOWN_URL_PATH = "/$$sermant$$/shutdown";

    /**
     * Success response code
     */
    public static final int GRACE_HTTP_SUCCESS_CODE = 200;

    /**
     * Failure response code
     */
    public static final int GRACE_HTTP_FAILURE_CODE = 500;

    /**
     * POST method
     */
    public static final String GRACE_HTTP_METHOD_POST = "POST";

    /**
     * Success response message
     */
    public static final String GRACE_OFFLINE_SUCCESS_MSG = "success";

    /**
     * Implement response messages
     */
    public static final String GRACE_FAILURE_MSG = "failed";

    /**
     * The KEY of the request for the offline notification
     */
    public static final String GRACE_OFFLINE_SOURCE_KEY = "sermant.grace.source";

    /**
     * The value of the request source of the offline notification
     */
    public static final String GRACE_OFFLINE_SOURCE_VALUE = "Sermant-agent";

    /**
     * The default maximum size of the upstream address of the cache
     */
    public static final long UPSTREAM_ADDRESS_DEFAULT_MAX_SIZE = 100L;

    /**
     * The default expiration time for the cache upstream address
     */
    public static final long UPSTREAM_ADDRESS_DEFAULT_EXPIRED_TIME = 60L;

    /**
     * Maximum port
     */
    public static final int MAX_HTTP_SERVER_PORT = 65535;

    /**
     * The maximum waiting time before going offline
     */
    public static final long MAX_SHUTDOWN_WAIT_TIME = 24 * 3600L;

    private GraceConstants() {
    }
}
