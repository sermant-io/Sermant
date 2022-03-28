/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.huawei.example.demo.interceptor;

import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.tracing.ExtractService;
import com.huawei.sermant.core.service.tracing.TracingService;
import com.huawei.sermant.core.service.tracing.common.TracingHeader;
import com.huawei.sermant.core.service.tracing.common.TracingRequest;

import java.util.HashMap;

/**
 * 链路监控功能的拦截器示例，本示例将展示如何在插件端使用链路监控功能
 * 该拦截器拦截provider数据，consumer数据见{@link DemoTraceConsumerInterceptor}
 *
 * @author luanwenfei
 * @version 1.0.0
 * @since 2022-03-18
 */
public class DemoTraceProviderInterceptor extends AbstractInterceptor {
    private final TracingService tracingService = ServiceManager.getService(TracingService.class);

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        TracingRequest request =
            new TracingRequest(context.getObject().getClass().getName(), context.getMethod().getName());
        ExtractService<HashMap<String, String>> extractService = (tracingRequest, carrier) -> {
            tracingRequest.setTraceId(carrier.get(TracingHeader.TRACE_ID.getValue()));
            tracingRequest.setParentSpanId(carrier.get(TracingHeader.PARENT_SPAN_ID.getValue()));
            tracingRequest.setSpanIdPrefix(carrier.get(TracingHeader.SPAN_ID_PREFIX.getValue()));
        };
        tracingService.onProviderSpanStart(request, extractService, (HashMap<String, String>)context.getResult());
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        tracingService.onSpanFinally();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        tracingService.onSpanError(context.getThrowable());
        return context;
    }
}
