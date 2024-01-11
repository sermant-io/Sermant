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

package com.huawei.metrics.interceptor.okhttp;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.interceptor.AbstractHttpInterceptor;
import com.huawei.metrics.manager.MetricsManager;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

/**
 * OKHttp请求发送增强器
 *
 * @author zhp
 * @since 2023-12-15
 */
public class CallInterceptor extends AbstractHttpInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        context.setLocalFieldValue(Constants.START_TIME_KEY, System.nanoTime());
        return context;
    }

    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) throws Exception {
        Response response = (Response) context.getResult();
        Request request = response.request();
        long latency = System.nanoTime() - (Long) context.getLocalFieldValue(Constants.START_TIME_KEY);
        MetricsRpcInfo metricsRpcInfo = initMetricsInfo(request.url(), request.isHttps(), latency, response.code());
        MetricsManager.saveRpcInfo(metricsRpcInfo);
        return context;
    }
}
