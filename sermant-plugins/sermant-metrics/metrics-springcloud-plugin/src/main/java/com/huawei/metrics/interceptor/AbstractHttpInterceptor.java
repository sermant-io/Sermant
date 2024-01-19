/*
 * Copyright (C) 2023-2024 Huawei Technologies Co., Ltd. All rights reserved.
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
import com.huawei.metrics.util.ResultJudgmentUtil;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * Http拦截器父类
 *
 * @author zhp
 * @since 2023-12-15
 */
public abstract class AbstractHttpInterceptor implements Interceptor {
    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        return collectMetrics(context);
    }

    /**
     * 采集指标信息
     *
     * @param context 上下文信息
     * @return ExecuteContext 上下文信息
     * @throws Exception 指标采集异常
     */
    public abstract ExecuteContext collectMetrics(ExecuteContext context) throws Exception;

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }

    /**
     * 初始化指标信息
     *
     * @param url 链接信息
     * @param enableSsl 是否开启SSL加密
     * @param latency 时延
     * @param statusCode 结果编码
     * @return 指标数据
     * @throws UnknownHostException 未知域名异常
     */
    public MetricsRpcInfo initMetricsInfo(URL url, boolean enableSsl, long latency, int statusCode)
            throws UnknownHostException {
        MetricsRpcInfo metricsRpcInfo = new MetricsRpcInfo();
        metricsRpcInfo.setClientIp(InetAddress.getLocalHost().getHostAddress());
        metricsRpcInfo.setServerIp(url.getHost());
        metricsRpcInfo.setServerPort(url.getPort());
        metricsRpcInfo.setProtocol(url.getProtocol());
        metricsRpcInfo.setEnableSsl(enableSsl);
        metricsRpcInfo.setL7Role(Constants.CLIENT_ROLE);
        metricsRpcInfo.setL4Role(Constants.TCP_PROTOCOL + Constants.CONNECT + metricsRpcInfo.getL7Role());
        metricsRpcInfo.setUrl(url.getPath());
        metricsRpcInfo.getReqCount().getAndIncrement();
        metricsRpcInfo.getResponseCount().getAndIncrement();
        metricsRpcInfo.getLatencyList().add(latency);
        metricsRpcInfo.getSumLatency().getAndAdd(latency);
        fillErrorCountInfo(statusCode, metricsRpcInfo);
        return metricsRpcInfo;
    }

    /**
     * 填充错误数量
     *
     * @param statusCode 状态编码
     * @param metricsRpcInfo 指标数据
     */
    public void fillErrorCountInfo(int statusCode, MetricsRpcInfo metricsRpcInfo) {
        int value = ResultJudgmentUtil.judgeHttpResult(statusCode);
        if (value == ResultType.SUCCESS.getValue()) {
            return;
        }
        metricsRpcInfo.getReqErrorCount().getAndIncrement();
        if (value == ResultType.CLIENT_ERROR.getValue()) {
            metricsRpcInfo.getClientErrorCount().getAndIncrement();
            return;
        }
        if (value == ResultType.SERVER_ERROR.getValue()) {
            metricsRpcInfo.getServerErrorCount().getAndIncrement();
        }
    }
}
