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

package io.sermant.flowcontrol.common.xds.retry;

import io.sermant.core.utils.StringUtils;
import io.sermant.flowcontrol.common.xds.retry.condition.ClientErrorCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.ConnectErrorRetryCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.GatewayErrorCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.ResetBeforeRequestErrorCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.ResetErrorCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.ServerErrorCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.SpecificHeaderNameErrorRetryCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.SpecificStatusCodeErrorRetryCondition;

import java.util.Optional;

/**
 * Retry Condition Manager
 *
 * @author zhp
 * @since 2024-11-29
 */
public enum RetryConditionType {
    /**
     * The type of conditional judgment for server errors
     */
    SERVER_ERROR("5xx", new ServerErrorCondition()),

    /**
     * The type of conditional judgment for client errors
     */
    CLIENT_ERROR("retriable-4xx", new ClientErrorCondition()),

    /**
     * The type of conditional judgment for gateway errors
     */
    GATEWAY_ERROR("gateway-error", new GatewayErrorCondition()),

    /**
     * The type of conditional judgment for reset errors
     */
    RESET_ERROR("reset", new ResetErrorCondition()),

    /**
     * The type of conditional judgment for resetting errors before request
     */
    RESET_BEFORE_REQUEST_ERROR("reset-before-request", new ResetBeforeRequestErrorCondition()),

    /**
     * The type of conditional judgment for connect errors
     */
    CONNECT_ERROR("connect-failure", new ConnectErrorRetryCondition()),

    /**
     * The type of conditional judgment for Specify response code
     */
    SPECIFIC_STATUS_CODE_ERROR("retriable-status-codes", new SpecificStatusCodeErrorRetryCondition()),

    /**
     * The type of conditional judgment for Specify response headers
     */
    SPECIFIC_HEADER_NAME_ERROR("retriable-headers", new SpecificHeaderNameErrorRetryCondition());

    /**
     * the name of retry condition
     */
    private final String conditionName;

    /**
     * the instance of implements class for retry condition
     */
    private final RetryCondition retryCondition;

    RetryConditionType(String conditionName, RetryCondition retryCondition) {
        this.conditionName = conditionName;
        this.retryCondition = retryCondition;
    }

    public String getConditionName() {
        return conditionName;
    }

    public RetryCondition getRetryCondition() {
        return retryCondition;
    }

    /**
     * get the instance of implements class by condition name
     *
     * @param conditionName condition name
     * @return instance of implements class for retry condition
     */
    public static Optional<RetryCondition> getRetryConditionByName(String conditionName) {
        for (RetryConditionType retryConditionType : RetryConditionType.values()) {
            if (StringUtils.equals(retryConditionType.getConditionName(), conditionName)) {
                return Optional.of(retryConditionType.getRetryCondition());
            }
        }
        return Optional.empty();
    }
}
