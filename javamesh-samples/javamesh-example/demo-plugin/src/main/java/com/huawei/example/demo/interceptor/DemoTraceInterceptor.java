/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.example.demo.interceptor;

import java.lang.reflect.Method;
import java.util.Map;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.StaticMethodInterceptor;
import com.huawei.apm.bootstrap.lubanops.trace.SpanEvent;
import com.huawei.apm.bootstrap.lubanops.trace.StartTraceRequest;
import com.huawei.apm.bootstrap.lubanops.trace.TraceCollector;

/**
 * 链路监控功能的拦截器示例，本示例将展示如何在插件端使用链路监控功能
 * <p>该功能在lubanops的插件中有完整实现，诸如http、dubbo、alidubbo、kafka等通信组件都有增强实现，无需重复开发
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
            final Map message = (Map) arguments[0];
            final Object traceId = message.get("traceId");
            final Object spanId = message.get("spanId");
            final Object gTraceId = message.get("gTraceId");
            final StartTraceRequest startTraceRequest = new StartTraceRequest(
                    clazz.getName(), method.getName(),
                    traceId == null ? null : traceId.toString(),
                    spanId == null ? null : spanId.toString(),
                    gTraceId == null ? null : gTraceId.toString());
            startTraceRequest.setDomainId("0");
            TraceCollector.onStart(startTraceRequest);
        }
    }

    @Override
    public Object after(Class<?> clazz, Method method, Object[] arguments, Object result) {
        if ("receive".equals(method.getName()) && result instanceof Map) {
            final SpanEvent spanEvent = TraceCollector.onStart(clazz.getName(), method.getName(), "DEMO_CONSUME");
            if (spanEvent != null) {
                final Map message = (Map) result;
                message.put("traceId", spanEvent.getTraceId());
                message.put("spanId", spanEvent.generateNextSpanId());
                message.put("gTraceId", spanEvent.getGlobalTraceId());
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
