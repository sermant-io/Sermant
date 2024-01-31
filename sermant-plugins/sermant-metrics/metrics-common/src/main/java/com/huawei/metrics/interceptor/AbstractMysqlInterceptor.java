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
import com.huawei.metrics.common.ResultType;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.util.InetAddressUtil;
import com.huawei.metrics.util.ResultJudgmentUtil;
import com.huawei.metrics.util.SqlParseUtil;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * MYSQL拦截器父类
 *
 * @author zhp
 * @since 2024-01-15
 */
public abstract class AbstractMysqlInterceptor implements Interceptor {
    /**
     * 日志
     */
    public static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return collectMetrics(context);
    }

    /**
     * 采集指标信息
     *
     * @param context 上下文信息
     * @return ExecuteContext 上下文信息
     */
    public abstract ExecuteContext collectMetrics(ExecuteContext context);

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }

    /**
     * 初始化指标数据
     *
     * @param context 上下文信息
     * @param enableSsl 是否开启SSL
     * @param host 服务端域名或者IP
     * @param serverPort 服务端端口
     * @param sql 执行的SQL
     * @return 指标信息
     */
    public MetricsRpcInfo initMetricsRpcInfo(ExecuteContext context, boolean enableSsl, String host,
            int serverPort, String sql) {
        MetricsRpcInfo metricsRpcInfo = new MetricsRpcInfo();
        metricsRpcInfo.setClientIp(InetAddressUtil.getHostAddress());
        metricsRpcInfo.setServerIp(InetAddressUtil.getHostAddress(host));
        metricsRpcInfo.setServerPort(serverPort);
        metricsRpcInfo.setProtocol(Constants.MYSQL_PROTOCOL);
        metricsRpcInfo.setEnableSsl(enableSsl);
        metricsRpcInfo.setL7Role(Constants.CLIENT_ROLE);
        metricsRpcInfo.setL4Role(Constants.TCP_PROTOCOL + Constants.CONNECT + metricsRpcInfo.getL7Role());
        metricsRpcInfo.getReqCount().getAndIncrement();
        metricsRpcInfo.getResponseCount().getAndIncrement();
        metricsRpcInfo.setUrl(SqlParseUtil.getApi(sql));
        if (context.getThrowable() == null) {
            return metricsRpcInfo;
        }
        metricsRpcInfo.getReqErrorCount().getAndIncrement();
        if (!(context.getThrowable() instanceof SQLException)) {
            return metricsRpcInfo;
        }
        SQLException exception = (SQLException) context.getThrowable();
        int value = ResultJudgmentUtil.judgeMysqlResult(exception.getErrorCode());
        if (ResultType.CLIENT_ERROR.getValue() == value) {
            metricsRpcInfo.getClientErrorCount().getAndIncrement();
        } else if (ResultType.SERVER_ERROR.getValue() == value) {
            metricsRpcInfo.getServerErrorCount().getAndIncrement();
        }
        return metricsRpcInfo;
    }
}
