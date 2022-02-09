/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.example.demo.interceptor;

import com.huawei.sermant.core.lubanops.bootstrap.trace.Headers;
import com.huawei.sermant.core.lubanops.bootstrap.trace.SpanEvent;
import com.huawei.sermant.core.lubanops.bootstrap.trace.TraceCollector;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.AbstractInterceptor;

import java.util.Map;

/**
 * 链路监控功能的拦截器示例，本示例将展示如何在插件端使用链路监控功能
 * <p>该拦截器拦截receive数据，send数据见{@link DemoTraceSendInterceptor}
 * <p>该功能在luban的插件中有完整实现，诸如http、dubbo、alidubbo、kafka等通信组件都有增强实现，无需重复开发
 * <p>本示例仅做示范，不建议开发者重新再插件端开发链路监控功能，如果可以，建议直接使用lubanops的插件
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-10-25
 */
public class DemoTraceReceiveInterceptor extends AbstractInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        final SpanEvent spanEvent = TraceCollector.onStart(
                context.getRawCls().getName(), context.getMethod().getName(), "DEMO_RECEIVE");
        final Map message = (Map) context.getResult();
        if (spanEvent != null) {
            message.put(Headers.TRACE_ID.getValue(), spanEvent.getTraceId());
            message.put(Headers.SPAN_ID.getValue(), spanEvent.generateNextSpanId());

            // 在这里添加其他tag

        } else {
            String globalTraceId = TraceCollector.getVirtualTraceId();
            if (globalTraceId != null) {
                message.put(Headers.GTRACE_ID.getValue(), globalTraceId);
            }
        }
        TraceCollector.onFinally();
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        TraceCollector.onError(context.getThrowable());
        return context;
    }
}
