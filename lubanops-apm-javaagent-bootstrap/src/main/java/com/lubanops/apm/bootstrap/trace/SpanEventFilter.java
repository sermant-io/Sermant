package com.lubanops.apm.bootstrap.trace;

/**
 * 描述可以对 {@link SpanEvent} 进行过滤的过滤器
 */
public interface SpanEventFilter {
    /**
     * 对该 {@link SpanEvent} 进行过滤
     *
     * @param spanEvent {@link SpanEvent} 对象
     * @return true：数据保留；false：数据丢弃
     */
    boolean doFilter(SpanEvent spanEvent);
}
