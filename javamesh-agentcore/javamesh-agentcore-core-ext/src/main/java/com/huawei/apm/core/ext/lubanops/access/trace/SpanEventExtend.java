package com.huawei.apm.core.ext.lubanops.access.trace;

import com.huawei.apm.core.ext.lubanops.access.inbound.EventDataBody;
import com.huawei.apm.core.ext.lubanops.access.inbound.EventDataHeader;

/**
 * 此类集成自SpanEventReport ，主要包含一些需要后期计算的字段，用此类反序列化，对象，可以减少对象转换成本 <br>
 *
 * @author
 * @since 2020年3月4日
 */
public class SpanEventExtend extends SpanEventReport {

    /**
     * 将上报的header和body信息拼接成一个对象
     *
     * @param header
     * @param body
     * @return
     */
    public static SpanEventExtend build(EventDataHeader header, EventDataBody body) {
        SpanEventExtend event = new SpanEventExtend();
        event.setInstanceId(header.getInstanceId());
        event.setAppId(header.getAppId());
        event.setBizId(header.getBizId());
        event.setEnvId(header.getEnvId());
        event.setDomainId(header.getDomainId());
        event.setAttachment(header.getAttachment());
        event.setStartTime(body.getStartTime());
        event.setTimeUsed(body.getTimeUsed());
        event.setTags(body.getTags());
        event.setAsync(body.isAsync());
        event.setClassName(body.getClassName());
        event.setMethod(body.getMethod());
        event.setTraceId(body.getTraceId());
        event.setGlobalTraceId(body.getGlobalTraceId());
        event.setGlobalPath(body.getGlobalPath());
        event.setSpanId(body.getSpanId());
        event.setEventId(body.getEventId());
        event.setHasError(body.getHasError());
        event.setErrorReasons(body.getErrorReasons());
        event.setChildrenEventCount(body.getChildrenEventCount());
        event.setNextSpanId(body.getNextSpanId());
        event.setType(body.getType());
        event.setSource(body.getSource());
        event.setRealSource(body.getRealSource());
        event.setArgument(body.getArgument());
        event.populate();

        return event;

    }

    /**
     * 根据属性计算 <br>
     *
     * @author
     * @since 2020年3月4日
     */
    public void populate() {

    }

}
