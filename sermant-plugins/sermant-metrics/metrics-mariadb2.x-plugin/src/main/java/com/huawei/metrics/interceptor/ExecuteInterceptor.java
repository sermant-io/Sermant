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
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.manager.MetricsManager;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol;
import org.mariadb.jdbc.internal.util.dao.ClientPrepareResult;

/**
 * Mariadb2.x SQL执行增强器
 *
 * @author zhp
 * @since 2024-01-15
 */
public class ExecuteInterceptor extends AbstractMysqlInterceptor {
    private static final int SQL_PARAM_INDEX = 2;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.setLocalFieldValue(Constants.START_TIME_KEY, System.nanoTime());
        return context;
    }

    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) {
        String sql = null;
        if (context.getArguments()[SQL_PARAM_INDEX] instanceof String) {
            sql = (String) context.getArguments()[SQL_PARAM_INDEX];
        } else if (context.getArguments()[SQL_PARAM_INDEX] instanceof ClientPrepareResult) {
            ClientPrepareResult clientPrepareResult = (ClientPrepareResult) context.getArguments()[SQL_PARAM_INDEX];
            sql = clientPrepareResult.getSql();
        }
        AbstractQueryProtocol protocol = (AbstractQueryProtocol) context.getObject();
        if (protocol.getSocket() == null || protocol.getSocket().getLocalAddress() == null
                || protocol.getOptions() == null) {
            return context;
        }
        MetricsRpcInfo metricsRpcInfo = initMetricsRpcInfo(context, protocol.getOptions().useSsl,
                protocol.getSocket().getLocalAddress().getHostAddress(), protocol.getPort(), sql);
        long latency = System.nanoTime() - (long) context.getLocalFieldValue(Constants.START_TIME_KEY);
        metricsRpcInfo.getSumLatency().getAndAdd(latency);
        metricsRpcInfo.getLatencyList().add(latency);
        MetricsManager.saveRpcInfo(metricsRpcInfo);
        return context;
    }
}
