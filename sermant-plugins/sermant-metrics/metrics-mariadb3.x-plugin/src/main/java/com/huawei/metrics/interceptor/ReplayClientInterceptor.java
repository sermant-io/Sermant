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

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.mariadb.jdbc.client.impl.TransactionSaver;
import org.mariadb.jdbc.message.client.RedoableClientMessage;
import org.mariadb.jdbc.message.client.RedoableWithPrepareClientMessage;

/**
 * mariadb3.x SQL执行增强器
 *
 * @author zhp
 * @since 2024-01-15
 */
public class ReplayClientInterceptor extends AbstractStandardClientExecuteInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.setLocalFieldValue(Constants.START_TIME_KEY, System.nanoTime());
        return context;
    }

    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) {
        Object startTime = context.getLocalFieldValue(Constants.START_TIME_KEY);
        if (context.getArguments()[0] == null || startTime == null) {
            return context;
        }
        TransactionSaver transactionSaver = (TransactionSaver) context.getArguments()[0];
        int length = transactionSaver.getIdx();
        if (length == 0) {
            return context;
        }
        double latency = (double) System.nanoTime() - (double) startTime;
        long avgLatency = Math.round(latency / (double) length);
        for (int i = 0; i < length; i++) {
            RedoableClientMessage querySaver = transactionSaver.getBuffers()[i];
            RedoableWithPrepareClientMessage message = (RedoableWithPrepareClientMessage) querySaver;
            String sql = message.getCommand();
            saveMetricInfo(context, sql, avgLatency);
        }
        return context;
    }
}
