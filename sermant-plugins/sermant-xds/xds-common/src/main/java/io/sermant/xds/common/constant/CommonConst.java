/*
 * Copyright (C) 2022-2025 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.xds.common.constant;

/**
 * constant class
 *
 * @author zhouss
 * @since 2022-01-28
 */
public class CommonConst {
    /**
     * http too many request exception codes
     */
    public static final int TOO_MANY_REQUEST_CODE = 429;

    /**
     * service exception
     */
    public static final int INTERVAL_SERVER_ERROR = 500;

    /**
     * the connect for request address
     */
    public static final String CONNECT = ":";

    /**
     * point
     */
    public static final String ESCAPED_POINT = "\\.";

    /**
     * Default response status code
     */
    public static final int DEFAULT_RESPONSE_CODE = -1;

    /**
     * the key of Scenario information for flow control
     */
    public static final String SCENARIO_INFO = "flowControlScenario";

    /**
     * the key of request-information
     */
    public static final String REQUEST_INFO = "REQUEST_INFO";

    /**
     * the default contentType
     */
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";

    /**
     * Minimum response code for a successful request
     */
    public static final int MIN_SUCCESS_STATUS_CODE = 200;

    /**
     * Maximum response code for a successful request
     */
    public static final int MAX_SUCCESS_STATUS_CODE = 399;

    /**
     * Retry condition based on the result
     */
    public static final int RETRY_CONDITION_BY_RESULT = 0;

    /**
     * Retry condition based on the exception
     */
    public static final int RETRY_CONDITION_BY_EXCEPTION = 1;

    /**
     * Retry condition based on both the status code and the exception
     */
    public static final int RETRY_CONDITION_BY_STATUS_AND_EXCEPTION = 2;

    private CommonConst() {
    }
}
