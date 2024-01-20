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

package com.huawei.metrics.interceptor.httpclient;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.interceptor.AbstractHttpInterceptor;
import com.huawei.metrics.manager.MetricsManager;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.StringUtils;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import java.net.URI;

/**
 * HttpClient请求发送增强器
 *
 * @author zhp
 * @since 2023-12-15
 */
public class HttpClientInterceptor extends AbstractHttpInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.setLocalFieldValue(Constants.START_TIME_KEY, System.nanoTime());
        return context;
    }

    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) throws Exception {
        HttpHost httpHost = (HttpHost) context.getArguments()[0];
        HttpRequest httpRequest = (HttpRequest) context.getArguments()[1];
        HttpResponse httpResponse = (HttpResponse) context.getResult();
        if (context.getLocalFieldValue(Constants.START_TIME_KEY) == null || httpRequest.getRequestLine() == null
                || httpResponse.getStatusLine() == null) {
            return context;
        }
        URI uri = new URI(httpRequest.getRequestLine().getUri());
        long latency = System.nanoTime() - (Long) context.getLocalFieldValue(Constants.START_TIME_KEY);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        boolean enableSsl = StringUtils.equals(httpHost.getSchemeName(), Constants.HTTPS_PROTOCOL);
        MetricsRpcInfo metricsRpcInfo = initMetricsInfo(uri.toURL(), enableSsl, latency, statusCode);
        MetricsManager.saveRpcInfo(metricsRpcInfo);
        return context;
    }
}
