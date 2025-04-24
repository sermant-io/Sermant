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

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.xds.common.config.XdsTrafficManagementConfig;
import io.sermant.xds.common.flowcontrol.retry.Retry;

/**
 * Retry condition check, determine if the response status code matches the specified status code, and trigger a retry
 * if it does.
 *
 * @author zhp
 * @since 2024-11-29
 */
public class RetriableStatusCodesRetryCondition implements RetryCondition {
    private static final XdsTrafficManagementConfig CONFIG = PluginConfigManager.getPluginConfig(
            XdsTrafficManagementConfig.class);

    @Override
    public boolean isNeedRetry(Retry retry, Throwable ex, String statusCode, Object result) {
        if (CollectionUtils.isEmpty(CONFIG.getRetryStatusCodes()) || StringUtils.isEmpty(statusCode)) {
            return false;
        }
        return CONFIG.getRetryStatusCodes().contains(statusCode);
    }
}
