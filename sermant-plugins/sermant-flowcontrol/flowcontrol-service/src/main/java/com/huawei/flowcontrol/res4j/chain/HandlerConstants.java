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

package com.huawei.flowcontrol.res4j.chain;

/**
 * handler constant
 *
 * @author zhouss
 * @since 2022-07-05
 */
public class HandlerConstants {
    /**
     * monitoring priority
     */
    public static final int MONITOR_ORDER = -2000;

    /**
     * service processing priority
     */
    public static final int BUSINESS_ORDER = -1000;

    /**
     * fault injection priority
     */
    public static final int FAULT_ORDER = 3000;

    /**
     * rate limiting priority
     */
    public static final int RATE_LIMIT_ORDER = 4000;

    /**
     * isolation bin priority
     */
    public static final int BULK_HEAD_ORDER = 5000;

    /**
     * Instance isolation priority, which must be greater than the circuit breaker priority
     */
    public static final int INSTANCE_ISOLATION_ORDER = 9000;

    /**
     * circuit breaker priority
     */
    public static final int CIRCUIT_BREAKER_ORDER = 10000;

    /**
     * system rule flow control priority
     */
    public static final int SYSTEM_RULE_FLOW_CONTROL = 11000;

    /**
     * Flags whether the current thread has a flow control exception
     */
    public static final String OCCURRED_FLOW_EXCEPTION = "__OCCURRED_FLOW_EXCEPTION__";

    /**
     * Flags whether the current thread triggers a request exception
     */
    public static final String OCCURRED_REQUEST_EXCEPTION = "__OCCURRED_REQUEST_EXCEPTION__";

    /**
     * thread variable provider key prefix
     */
    public static final String THREAD_LOCAL_DUBBO_PROVIDER_PREFIX = "PROVIDER:";

    /**
     * thread variable consumer key prefix
     */
    public static final String THREAD_LOCAL_DUBBO_CONSUMER_PREFIX = "CONSUMER:";

    /**
     * key prefix
     */
    public static final String THREAD_LOCAL_KEY_PREFIX = HandlerConstants.class.getName()
            + "___THREAD_LOCAL_KEY_PREFIX___";

    private HandlerConstants() {
    }
}
