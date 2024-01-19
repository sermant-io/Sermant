/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.interceptor.servlet;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.interceptor.AbstractHttpInterceptor;
import com.huawei.metrics.manager.MetricsManager;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttpServlet服务调用拦截声明
 *
 * @author zhp
 * @since 2023-12-15
 */
public class HttpServletInterceptor extends AbstractHttpInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.setLocalFieldValue(Constants.START_TIME_KEY, System.nanoTime());
        return context;
    }

    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) {
        HttpServletRequest req = (HttpServletRequest) context.getArguments()[0];
        long latency = System.nanoTime() - (Long) context.getLocalFieldValue(Constants.START_TIME_KEY);
        HttpServletResponse resp = (HttpServletResponse) context.getArguments()[1];
        MetricsRpcInfo metricsRpcInfo = initMetricsInfo(req, latency, resp.getStatus());
        MetricsManager.saveRpcInfo(metricsRpcInfo);
        return context;
    }

    /**
     * 初始化指标信息
     *
     * @param req 请求数据
     * @param latency 时延
     * @param status 请求状态编码
     * @return Rpc指标数据
     */
    private MetricsRpcInfo initMetricsInfo(HttpServletRequest req, long latency, int status) {
        MetricsRpcInfo metricsRpcInfo = new MetricsRpcInfo();
        metricsRpcInfo.setClientIp(req.getRemoteHost());
        metricsRpcInfo.setServerIp(req.getLocalAddr());
        metricsRpcInfo.setServerPort(req.getLocalPort());
        metricsRpcInfo.setProtocol(req.getScheme());
        metricsRpcInfo.setEnableSsl(Constants.HTTPS_PROTOCOL.equals(req.getScheme()));
        metricsRpcInfo.setL7Role(Constants.SERVER_ROLE);
        metricsRpcInfo.setL4Role(Constants.TCP_PROTOCOL + Constants.CONNECT + metricsRpcInfo.getL7Role());
        metricsRpcInfo.setUrl(req.getRequestURI());
        metricsRpcInfo.getReqCount().getAndIncrement();
        metricsRpcInfo.getLatencyList().add(latency);
        metricsRpcInfo.getSumLatency().getAndAdd(latency);
        metricsRpcInfo.getResponseCount().getAndIncrement();
        fillErrorCountInfo(status, metricsRpcInfo);
        return metricsRpcInfo;
    }
}
