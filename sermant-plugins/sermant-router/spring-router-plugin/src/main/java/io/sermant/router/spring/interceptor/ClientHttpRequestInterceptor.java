/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.utils.LogUtils;
import io.sermant.router.common.config.RouterConfig;
import io.sermant.router.common.metric.MetricThreadLocal;
import io.sermant.router.common.metric.MetricsManager;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.request.RequestTag;
import io.sermant.router.common.utils.ThreadLocalUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ClientHttpRequestInterceptor enhancement class, initiate restTemplate request method
 *
 * @author provenceee
 * @since 2022-07-12
 */
public class ClientHttpRequestInterceptor extends AbstractInterceptor {
    private final RouterConfig routerConfig = PluginConfigManager.getPluginConfig(RouterConfig.class);

    @Override
    public ExecuteContext before(ExecuteContext context) {
        LogUtils.printHttpRequestBeforePoint(context);
        Object obj = context.getObject();
        if (obj instanceof HttpRequest && ThreadLocalUtils.getRequestData() == null) {
            HttpRequest request = (HttpRequest) obj;
            HttpHeaders headers = request.getHeaders();
            putIfAbsent(headers);
            String path = request.getURI().getPath();
            ThreadLocalUtils.setRequestData(new RequestData(headers, path, request.getMethod().name()));
        }
        MetricThreadLocal.setFlag(true);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        collectRequestCountMetric(context);
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    private void collectRequestCountMetric(ExecuteContext context) {
        // To prevent execution twice, after mounting and registering the plugin, this interception point is
        // executed twice for a single request
        Object obj = context.getObject();
        if (routerConfig.isEnableMetric() && MetricThreadLocal.getFlag() && obj instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) obj;
            MetricsManager.collectRequestCountMetric(request.getURI());
        }
        MetricThreadLocal.removeFlag();
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        ThreadLocalUtils.removeRequestData();
        LogUtils.printHttpRequestOnThrowPoint(context);
        MetricThreadLocal.removeFlag();
        return context;
    }

    private void putIfAbsent(HttpHeaders headers) {
        RequestTag requestTag = ThreadLocalUtils.getRequestTag();
        if (requestTag != null) {
            Map<String, List<String>> header = requestTag.getTag();
            for (Entry<String, List<String>> entry : header.entrySet()) {
                // Use the header passed upstream
                headers.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
    }
}
