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
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.mariadb.jdbc.HostAddress;
import org.mariadb.jdbc.client.impl.StandardClient;
import org.mariadb.jdbc.export.SslMode;
import org.mariadb.jdbc.message.ClientMessage;

/**
 * mysql-connector-java8.0 SQL执行增强器
 *
 * @author zhp
 * @since 2024-01-15
 */
public class StandardClientInterceptor extends AbstractMysqlInterceptor {
    private static final int PARAM_COUNT = 1;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.setLocalFieldValue(Constants.START_TIME_KEY, System.nanoTime());
        return context;
    }

    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) {
        if (context.getArguments()[0] == null) {
            return context;
        }
        ClientMessage message = (ClientMessage) context.getArguments()[0];
        String sql = message.description();
        if (StringUtils.isEmpty(sql)) {
            return context;
        }
        StandardClient client = (StandardClient) context.getObject();
        if (client.getContext() == null || client.getContext().getConf() == null || client.getHostAddress() == null) {
            return context;
        }
        boolean enableSsl = client.getContext() != null && client.getContext().getConf() != null
                && client.getContext().getConf().sslMode() != SslMode.DISABLE;
        HostAddress hostAddress = client.getHostAddress();
        MetricsRpcInfo metricsRpcInfo = initMetricsRpcInfo(context, enableSsl, hostAddress.host, hostAddress.port, sql);
        long latency = System.nanoTime() - (long) context.getLocalFieldValue(Constants.START_TIME_KEY);
        metricsRpcInfo.getSumLatency().getAndAdd(latency);
        metricsRpcInfo.getLatencyList().add(latency);
        MetricsManager.saveRpcInfo(metricsRpcInfo);
        return context;
    }
}
