/*
 *   Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *   the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *   an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 */

package com.huawei.example.demo.interceptor;

import com.huawei.example.demo.common.DemoLogger;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;
import com.huawei.sermant.core.service.ServiceManager;
import com.huawei.sermant.core.service.tracing.api.TracingService;
import com.huawei.sermant.core.service.tracing.common.SpanEvent;
import com.huawei.sermant.core.service.tracing.common.SpanEventContext;
import com.huawei.sermant.core.service.tracing.common.TracingRequest;

import java.util.Locale;
import java.util.Optional;

/**
 * 链路监控功能的拦截器实例，本示例将展示如何在插件端使用链路监控功能
 *
 * @author luanwenfei
 * @since 2022-04-07
 */
public class DemoTraceNormalInterceptor extends AbstractInterceptor {
    TracingService tracingService = ServiceManager.getService(TracingService.class);

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        TracingRequest request = new TracingRequest(context.getRawCls().getName(), context.getMethod().getName());
        tracingService.onNormalSpanStart(request);
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        Optional<SpanEventContext> spanEventContextOptional = tracingService.getContext();
        if (spanEventContextOptional.isPresent()) {
            SpanEvent spanEvent = spanEventContextOptional.get().getSpanEvent();
            DemoLogger.println(String.format(Locale.ROOT, "NormalSpanEvent TraceId: %s, SpanId: %s.",
                    spanEvent.getTraceId(), spanEvent.getSpanId()));
        }
        tracingService.onSpanFinally();
        return context;
    }
}
