/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.metrics.interceptor;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.manager.MetricsManager;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * Mariadb2.x SQL执行增强器
 *
 * @author zhp
 * @since 2024-01-15
 */
public class ExecutePreparedInterceptor extends AbstractQueryProtocolInterceptor {
    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) {
        Object startTime = context.getLocalFieldValue(Constants.START_TIME_KEY);
        if (context.getArguments()[0] == null || startTime == null) {
            return context;
        }
        String sql = (String) context.getArguments()[0];
        long latency = System.nanoTime() - (long) startTime;
        MetricsManager.saveRpcInfo(createRpcInfo(sql, context, latency));
        return context;
    }
}
