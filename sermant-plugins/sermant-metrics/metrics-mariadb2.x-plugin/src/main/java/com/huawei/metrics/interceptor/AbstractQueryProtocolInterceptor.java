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

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.mariadb.jdbc.internal.protocol.AbstractQueryProtocol;

/**
 * Mariadb2.x SQL执行增强器
 *
 * @author zhp
 * @since 2024-01-15
 */
public abstract class AbstractQueryProtocolInterceptor extends AbstractMysqlInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.setLocalFieldValue(Constants.START_TIME_KEY, System.nanoTime());
        return context;
    }

    /**
     * 创建RPC实体类
     *
     * @param sql SQL信息
     * @param context 上下文信息
     * @param latency 时延
     * @return 指标数据
     */
    public MetricsRpcInfo createRpcInfo(String sql, ExecuteContext context, long latency) {
        boolean enableSsl = false;
        AbstractQueryProtocol protocol = (AbstractQueryProtocol) context.getObject();
        if (protocol.getUrlParser() != null && protocol.getUrlParser() != null
                && protocol.getUrlParser().getOptions() != null) {
            enableSsl = protocol.getUrlParser().getOptions().useSsl;
        }
        MetricsRpcInfo metricsRpcInfo = initMetricsRpcInfo(context, enableSsl, protocol.getHost(), protocol.getPort(),
                sql);
        metricsRpcInfo.getSumLatency().getAndAdd(latency);
        return metricsRpcInfo;
    }
}
