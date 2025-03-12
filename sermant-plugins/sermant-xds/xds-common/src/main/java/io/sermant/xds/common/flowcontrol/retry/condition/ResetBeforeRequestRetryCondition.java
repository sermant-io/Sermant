/*
 * Copyright (C) 2024-2025 Sermant Authors. All rights reserved.
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

package io.sermant.xds.common.flowcontrol.retry.condition;

import io.sermant.xds.common.context.XdsTrafficManagementContext;
import io.sermant.xds.common.flowcontrol.retry.Retry;

/**
 * Retry condition check, determine if the current error is a connection reset error before the request, and trigger a
 * retry if it is.
 *
 * @author zhp
 * @since 2024-11-29
 */
public class ResetBeforeRequestRetryCondition extends ResetRetryCondition {
    @Override
    public boolean isNeedRetry(Retry retry, Throwable ex, String statusCode, Object result) {
        return XdsTrafficManagementContext.getSendByteFlag() && super.isNeedRetry(retry, ex, statusCode, result);
    }
}
