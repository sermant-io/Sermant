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

package com.huawei.metrics.interceptor.dubbo.apache;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.interceptor.AbstractCodecInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.remoting.Channel;
import org.apache.dubbo.remoting.exchange.Response;

/**
 * dubbo报文转码、解码拦截器
 *
 * @author zhp
 * @since 2023-10-17
 */
public class ExchangeCodecInterceptor extends AbstractCodecInterceptor {
    /**
     * 初始化指标信息
     *
     * @param context 上下文信息
     * @return 连接信息
     */
    private MetricsRpcInfo initRpcInfo(ExecuteContext context) {
        Channel channel = (Channel) context.getArguments()[0];
        URL url = channel.getUrl();
        boolean sslEnable = Boolean.parseBoolean(url.getParameter(Constants.SSL_ENABLE));
        MetricsRpcInfo metricsRpcInfo = initRpcInfo(channel.getLocalAddress(), channel.getRemoteAddress(),
                url.getParameter(Constants.SIDE_KEY), sslEnable, url.getProtocol());
        metricsRpcInfo.setUrl(url.getPath());
        return metricsRpcInfo;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        if (!(context.getResult() instanceof Response)) {
            return context;
        }
        Response response = (Response) context.getResult();
        if (response.isHeartbeat() || response.isEvent()) {
            return context;
        }
        MetricsRpcInfo metricsRpcInfo = initRpcInfo(context);
        fillErrorCountInfo(response.getStatus(), metricsRpcInfo);
        return context;
    }
}
