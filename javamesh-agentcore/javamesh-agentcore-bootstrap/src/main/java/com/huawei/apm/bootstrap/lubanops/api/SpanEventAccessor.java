package com.huawei.apm.bootstrap.lubanops.api;

import com.huawei.apm.bootstrap.lubanops.trace.SpanEvent;

/**
 * 异步调用链信息传递接口
 */
public interface SpanEventAccessor {
    SpanEvent getSpanEvent();

    void setSpanEvent(SpanEvent spanEvent);
}
