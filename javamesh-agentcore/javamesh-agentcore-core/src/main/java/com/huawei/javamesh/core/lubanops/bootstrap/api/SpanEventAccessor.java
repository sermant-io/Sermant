package com.huawei.javamesh.core.lubanops.bootstrap.api;

import com.huawei.javamesh.core.lubanops.bootstrap.trace.SpanEvent;

/**
 * 异步调用链信息传递接口
 */
public interface SpanEventAccessor {
    SpanEvent getSpanEvent();

    void setSpanEvent(SpanEvent spanEvent);
}
