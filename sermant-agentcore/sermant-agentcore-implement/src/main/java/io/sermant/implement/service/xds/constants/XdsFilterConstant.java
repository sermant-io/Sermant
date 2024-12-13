/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.implement.service.xds.constants;

/**
 * Xds Filter Constant
 *
 * @author zhp
 * @since 2024-12-11
 **/
public class XdsFilterConstant {
    /**
     * filter name of http fault
     */
    public static final String HTTP_FAULT_FILTER_NAME = "envoy.filters.http.fault";

    /**
     * filter name of http local rate limit filter
     */
    public static final String LOCAL_RATE_LIMIT_FILTER_FILTER_NAME = "envoy.filters.http.local_ratelimit";

    /**
     * the key of token bucket with Rate limit configuration
     */
    public static final String TOKEN_BUCKET = "token_bucket";

    /**
     * the key of Percentage of effective enforcement of ratelimit rule
     */
    public static final String FILTER_ENFORCED = "filter_enforced";

    /**
     * the key of Percentage of enforcement of ratelimit rule
     */
    public static final String FILTER_ENABLED = "filter_enabled";

    /**
     * The key of All configuration information for the response header to be added
     */
    public static final String RESPONSE_HEADERS_TO_ADD = "response_headers_to_add";

    /**
     * The key of the configuration for the response header
     */
    public static final String HEADER = "header";

    /**
     * The key of the append the response header
     */
    public static final String APPEND = "append";

    /**
     * The key of the name of response header
     */
    public static final String HEADER_KEY = "key";

    /**
     * The key of the value of response header
     */
    public static final String HEADER_VALUE = "value";

    /**
     * The key of the value for FractionalPercent
     */
    public static final String DEFAULT_VALUE = "default_value";

    /**
     * The key of the numerator for FractionalPercent
     */
    public static final String NUMERATOR = "numerator";

    /**
     * The key of the denominator for FractionalPercent
     */
    public static final String DENOMINATOR = "denominator";

    /**
     * The key of number for tokens filled each time
     */
    public static final String TOKENS_PER_FILL = "tokens_per_fill";

    /**
     * The Key for the time interval of token filling
     */
    public static final String FILL_INTERVAL = "fill_interval";

    /**
     * The Key for the maximum number of tokens
     */
    public static final String MAX_TOKENS = "max_tokens";

    /**
     * Parse Time prefix
     */
    public static final String PARSE_TIME_PREFIX = "PT";

    private XdsFilterConstant() {
    }
}
