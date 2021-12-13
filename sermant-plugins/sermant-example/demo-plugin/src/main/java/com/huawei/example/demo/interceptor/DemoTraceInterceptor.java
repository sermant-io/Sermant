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

import java.lang.reflect.Method;
import java.util.Map;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.StaticMethodInterceptor;
import com.huawei.sermant.core.lubanops.bootstrap.trace.Headers;
import com.huawei.sermant.core.lubanops.bootstrap.trace.SpanEvent;
import com.huawei.sermant.core.lubanops.bootstrap.trace.StartTraceRequest;
import com.huawei.sermant.core.lubanops.bootstrap.trace.TraceCollector;

/**
 * 链路监控功能的拦截器示例，本示例将展示如何在插件端使用链路监控功能
 * <p>该功能在luban的插件中有完整实现，诸如http、dubbo、alidubbo、kafka等通信组件都有增强实现，无需重复开发
 * <p>本示例仅做示范，不建议开发者重新再插件端开发链路监控功能，如果可以，建议直接使用lubanops的插件
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/10/25
 */
public class DemoTraceInterceptor implements StaticMethodInterceptor {
    @Override
    public void before(Class<?> clazz, Method method, Object[] arguments, BeforeResult beforeResult) {
        if ("send".equals(method.getName()) && arguments[0] instanceof Map) {
            // 消息传输载体，这里用Map举例
            final Map message = (Map) arguments[0];
            final Object traceId = message.get(Headers.TRACE_ID.getValue());
            final Object spanId = message.get(Headers.SPAN_ID.getValue());
            Object gTraceId = null;
            if (traceId == null || traceId.toString().trim().length() <= 0) {
                gTraceId = message.get(Headers.GTRACE_ID.getValue());
            }
            final StartTraceRequest startTraceRequest = new StartTraceRequest(
                    clazz.getName(), method.getName(),
                    traceId == null ? null : traceId.toString(),
                    spanId == null ? null : spanId.toString(),
                    gTraceId == null ? null : gTraceId.toString());
            startTraceRequest.setDomainId("0");
            startTraceRequest.setKind("DEMO_SEND");
            // 添加source等其他资源
            TraceCollector.onStart(startTraceRequest);
            // 添加其他tag
        }
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) {
        if ("receive".equals(method.getName()) && result instanceof Map) {
            final SpanEvent spanEvent = TraceCollector.onStart(clazz.getName(), method.getName(), "DEMO_RECEIVE");
            if (spanEvent != null) {
                final Map message = (Map) result;
                message.put(Headers.TRACE_ID.getValue(), spanEvent.getTraceId());
                message.put(Headers.SPAN_ID.getValue(), spanEvent.generateNextSpanId());
                // 添加其他tag
            } else {
                String gTraceId = TraceCollector.getVirtualTraceId();
                if (gTraceId != null) {
                    ((Map) result).put(Headers.GTRACE_ID.getValue(), gTraceId);
                }
            }
        }
        TraceCollector.onFinally();
        return result;
    }

    @Override
    public void onThrow(Class<?> clazz, Method method, Object[] arguments, Throwable t) {
        TraceCollector.onError(t);
    }
}
