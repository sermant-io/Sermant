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

import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.manager.MetricsManager;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.mariadb.jdbc.HostAddress;
import org.mariadb.jdbc.client.impl.StandardClient;
import org.mariadb.jdbc.export.SslMode;

/**
 * mariadb3.x SQL执行增强器
 *
 * @author zhp
 * @since 2024-01-15
 */
public abstract class AbstractStandardClientExecuteInterceptor extends AbstractMysqlInterceptor {
    /**
     * 保存指标数据
     *
     * @param context 上下文信息
     * @param sql sql信息
     * @param latency 时延
     * @return 上下文信息
     */
    public ExecuteContext saveMetricInfo(ExecuteContext context, String sql, long latency) {
        if (StringUtils.isEmpty(sql)) {
            LOGGER.warning("Unable to obtain the SQL that needs to be executed.");
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
        metricsRpcInfo.getSumLatency().getAndAdd(latency);
        metricsRpcInfo.getLatencyList().add(latency);
        MetricsManager.saveRpcInfo(metricsRpcInfo);
        return context;
    }
}
