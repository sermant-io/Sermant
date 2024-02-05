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
import com.huawei.metrics.util.ThreadMetricsUtil;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.StringUtils;

import com.mysql.jdbc.Connection;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mysql5.0.x SQL执行增强器
 *
 * @author zhp
 * @since 2024-01-15
 */
public class ConnectionInterceptor extends AbstractMysqlInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final int URI_START_INDEX = 5;

    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.setLocalFieldValue(Constants.START_TIME_KEY, System.nanoTime());
        return context;
    }

    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) {
        if (context.getArguments()[1] == null && StringUtils.isEmpty(ThreadMetricsUtil.getSql())) {
            LOGGER.log(Level.WARNING, "Unable to obtain the SQL that needs to be executed.");
            return context;
        }
        String sql;
        if (context.getArguments()[1] != null) {
            sql = (String) context.getArguments()[1];
        } else {
            sql = ThreadMetricsUtil.getSql();
        }
        try {
            Connection connection = (Connection) context.getObject();
            String jdbcUrl = connection.getMetaData().getURL();
            URI url = new URI(jdbcUrl.substring(URI_START_INDEX));
            MetricsRpcInfo metricsRpcInfo = initMetricsRpcInfo(context, connection.getUseSSL(),
                    url.getHost(), url.getPort(), sql);
            long latency = System.nanoTime() - (long) context.getLocalFieldValue(Constants.START_TIME_KEY);
            metricsRpcInfo.getSumLatency().getAndAdd(latency);
            MetricsManager.saveRpcInfo(metricsRpcInfo);
            return context;
        } catch (SQLException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Can not get connect url.", e);
            return context;
        }
    }
}
