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

import org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol;
import org.mariadb.jdbc.internal.util.dao.ClientPrepareResult;
import org.mariadb.jdbc.internal.util.dao.ServerPrepareResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Mariadb2.x SQL批量执行增强器
 *
 * @author zhp
 * @since 2024-01-15
 */
public class ExecuteBatchInterceptor extends AbstractMysqlInterceptor {
    private static final int SQL_PARAM_INDEX = 2;

    private static final int SERVER_PREPARE_RESULT_INDEX = 1;

    private static final int CLIENT_PREPARE_RESULT_INDEX = 2;

    private static final int ENABLE_NUM = 2;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.setLocalFieldValue(Constants.START_TIME_KEY, System.nanoTime());
        return context;
    }

    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) {
        List<String> sqlList = getSqlList(context);
        if (sqlList.isEmpty()) {
            return context;
        }
        AbstractQueryProtocol protocol = (AbstractQueryProtocol) context.getObject();
        if (protocol.getSocket() == null || protocol.getSocket().getLocalAddress() == null
                || protocol.getOptions() == null) {
            return context;
        }
        long latency = System.nanoTime() - (long) context.getLocalFieldValue(Constants.START_TIME_KEY);
        long avgLatency = new BigDecimal(latency).divide(new BigDecimal(sqlList.size()), ENABLE_NUM,
                RoundingMode.HALF_UP).longValue();
        for (String sql : sqlList) {
            MetricsRpcInfo metricsRpcInfo = initMetricsRpcInfo(context, protocol.getOptions().useSsl,
                    protocol.getSocket().getLocalAddress().getHostAddress(), protocol.getPort(), sql);
            metricsRpcInfo.getSumLatency().getAndAdd(avgLatency);
            metricsRpcInfo.getLatencyList().add(avgLatency);
            MetricsManager.saveRpcInfo(metricsRpcInfo);
        }
        return context;
    }

    /**
     * 获取SQL数据
     *
     * @param context 上下文信息
     * @return SQL信息
     */
    private List<String> getSqlList(ExecuteContext context) {
        List<String> sqlList = new ArrayList<>();
        String methodName = context.getMethod().getName();
        if (StringUtils.equals(methodName, "executeBatchStmt")) {
            List<String> queries = (List<String>) context.getArguments()[SQL_PARAM_INDEX];
            sqlList.addAll(queries);
        } else if (StringUtils.equals(methodName, "executeBatchServer")) {
            ServerPrepareResult result = (ServerPrepareResult) context.getArguments()[SERVER_PREPARE_RESULT_INDEX];
            sqlList.add(result.getSql());
        } else {
            ClientPrepareResult result = (ClientPrepareResult) context.getArguments()[CLIENT_PREPARE_RESULT_INDEX];
            sqlList.add(result.getSql());
        }
        return sqlList;
    }
}
