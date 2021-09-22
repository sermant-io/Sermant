package com.lubanops.apm.bootstrap.api;

import com.lubanops.apm.bootstrap.trace.SpanEvent;

/**
 * 异步调用链信息传递接口
 */
public interface SpanEventAccessor {
    SpanEvent getSpanEvent();

    void setSpanEvent(SpanEvent spanEvent);
}
