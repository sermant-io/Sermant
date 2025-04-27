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

package io.sermant.xds.common.flowcontrol.retry;

import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.xds.common.constant.CommonConst;
import io.sermant.xds.common.flowcontrol.retry.condition.RetryCondition;
import io.sermant.xds.common.flowcontrol.retry.condition.RetryConditionType;
import io.sermant.xds.common.flowcontrol.retry.policy.RetryPolicy;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * retryAbstraction，provide a common method
 *
 * @author zhouss
 * @since 2022-02-10
 */
public abstract class AbstractRetry implements Retry {
    @Override
    public boolean isNeedRetry(Object result) {
        if (result == null) {
            return false;
        }
        RetryPolicy retryPolicy = RetryContext.INSTANCE.getRetryPolicy();
        if (retryPolicy == null) {
            return false;
        }
        List<String> conditions = retryPolicy.getRetryConditions();
        if (CollectionUtils.isEmpty(conditions)) {
            return false;
        }
        Optional<String> statusCodeOptional = this.getStatusCode(result);
        if (!statusCodeOptional.isPresent()) {
            return false;
        }
        String statusCode = statusCodeOptional.get();
        if (isSuccess(statusCode)) {
            return false;
        }
        for (String conditionName : conditions) {
            Optional<RetryCondition> retryConditionOptional = RetryConditionType
                    .getRetryConditionWithResultByName(conditionName);
            if (!retryConditionOptional.isPresent()) {
                continue;
            }
            if (retryConditionOptional.get().isNeedRetry(this, null, statusCode, result)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNeedRetry(Throwable ex) {
        if (ex == null) {
            return false;
        }
        RetryPolicy retryPolicy = RetryContext.INSTANCE.getRetryPolicy();
        if (retryPolicy == null) {
            return false;
        }
        for (String conditionName : retryPolicy.getRetryConditions()) {
            Optional<RetryCondition> retryConditionOptional = RetryConditionType
                    .getRetryConditionWithExceptionByName(conditionName);
            if (!retryConditionOptional.isPresent()) {
                continue;
            }
            if (retryConditionOptional.get().isNeedRetry(this, ex, null, null)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if the request is successful
     *
     * @param statusCode status code
     * @return if the request is successful,true : success false: failure
     */
    protected static boolean isSuccess(String statusCode) {
        if (StringUtils.isEmpty(statusCode)) {
            return false;
        }
        int code = Integer.parseInt(statusCode);
        return code >= CommonConst.MIN_SUCCESS_STATUS_CODE && code <= CommonConst.MAX_SUCCESS_STATUS_CODE;
    }

    /**
     * implemented by subclasses， if subclass implement, no need to implement the get code method
     *
     * @param result interface response result
     * @return response status code
     * @throws UnsupportedOperationException unsupported operation
     */
    public Optional<String> getStatusCode(Object result) {
        return Optional.empty();
    }

    /**
     * Get the name of the response header in the response information
     *
     * @param result interface response result
     * @return response header names
     * @throws UnsupportedOperationException unsupported operation
     */
    public Optional<Set<String>> getHeaderNames(Object result) {
        return Optional.empty();
    }
}
