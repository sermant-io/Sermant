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

import io.sermant.flowcontrol.common.xds.retry.condition.ClientErrorCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.ConnectErrorRetryCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.GatewayErrorCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.ResetBeforeRequestErrorCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.ResetErrorCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.ServerErrorCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.SpecificHeaderNameErrorRetryCondition;
import io.sermant.flowcontrol.common.xds.retry.condition.SpecificStatusCodeErrorRetryCondition;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

/**
 * RetryConditionManagerTest
 *
 * @author zhp
 * @since 2024-11-29
 */
public class RetryConditionManagerTest {
    @Test
    public void testRetryCondition() {
        Optional<RetryCondition> result = RetryConditionType.getRetryConditionByName(
                RetryConditionType.SERVER_ERROR.getConditionName());
        assertTrue(result.isPresent() && result.get() instanceof ServerErrorCondition);
        result = RetryConditionType.getRetryConditionByName(
                RetryConditionType.CLIENT_ERROR.getConditionName());
        assertTrue(result.isPresent() && result.get() instanceof ClientErrorCondition);
        result = RetryConditionType.getRetryConditionByName(
                RetryConditionType.CONNECT_ERROR.getConditionName());
        assertTrue(result.isPresent() && result.get() instanceof ConnectErrorRetryCondition);
        result = RetryConditionType.getRetryConditionByName(
                RetryConditionType.RESET_BEFORE_REQUEST_ERROR.getConditionName());
        assertTrue(result.isPresent() && result.get() instanceof ResetBeforeRequestErrorCondition);
        result = RetryConditionType.getRetryConditionByName(
                RetryConditionType.GATEWAY_ERROR.getConditionName());
        assertTrue(result.isPresent() && result.get() instanceof GatewayErrorCondition);
        result = RetryConditionType.getRetryConditionByName(
                RetryConditionType.RESET_ERROR.getConditionName());
        assertTrue(result.isPresent() && result.get() instanceof ResetErrorCondition);
        result = RetryConditionType.getRetryConditionByName(
                RetryConditionType.SPECIFIC_HEADER_NAME_ERROR.getConditionName());
        assertTrue(result.isPresent() && result.get() instanceof SpecificHeaderNameErrorRetryCondition);
        result = RetryConditionType.getRetryConditionByName(
                RetryConditionType.SPECIFIC_STATUS_CODE_ERROR.getConditionName());
        assertTrue(result.isPresent() && result.get() instanceof SpecificStatusCodeErrorRetryCondition);
    }
}
