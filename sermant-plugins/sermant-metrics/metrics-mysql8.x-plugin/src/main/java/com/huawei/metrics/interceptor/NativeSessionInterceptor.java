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

import com.mysql.cj.NativeSession;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.jdbc.JdbcPreparedStatement;

/**
 * mysql-connector-java8.0 SQL执行增强器
 *
 * @author zhp
 * @since 2024-01-15
 */
public class NativeSessionInterceptor extends AbstractMysqlInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.setLocalFieldValue(Constants.START_TIME_KEY, System.nanoTime());
        return context;
    }

    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) {
        Object startTime = context.getLocalFieldValue(Constants.START_TIME_KEY);
        if (startTime == null) {
            return context;
        }
        String sql;
        Object object = context.getArguments()[0];
        if (object instanceof JdbcPreparedStatement) {
            sql = ((JdbcPreparedStatement) object).getPreparedSql();
        } else {
            sql = (String) context.getArguments()[1];
        }
        if (StringUtils.isEmpty(sql)) {
            LOGGER.warning("Unable to obtain the SQL that needs to be executed.");
            return context;
        }
        NativeSession nativeSession = (NativeSession) context.getObject();
        HostInfo hostInfo = nativeSession.getHostInfo();
        if (hostInfo == null) {
            LOGGER.warning("Unable to obtain host information for database.");
            return context;
        }
        MetricsRpcInfo metricsRpcInfo = initMetricsRpcInfo(context, nativeSession.isSSLEstablished(),
                hostInfo.getHost(), hostInfo.getPort(), sql);
        long latency = System.nanoTime() - (long) startTime;
        metricsRpcInfo.getSumLatency().getAndAdd(latency);
        metricsRpcInfo.getLatencyList().add(latency);
        MetricsManager.saveRpcInfo(metricsRpcInfo);
        return context;
    }
}
