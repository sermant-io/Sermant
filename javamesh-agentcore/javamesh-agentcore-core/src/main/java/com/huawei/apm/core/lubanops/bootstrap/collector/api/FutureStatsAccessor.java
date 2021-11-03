package com.huawei.apm.core.lubanops.bootstrap.collector.api;


import com.huawei.apm.core.lubanops.bootstrap.api.SpanEventAccessor;
import com.huawei.apm.core.lubanops.bootstrap.trace.SpanEvent;

/**
 * @author
 */
public class FutureStatsAccessor implements SpanEventAccessor {
    private String serviceName;

    private String method;

    private String envId;

    private long startTime;

    private SpanEvent spanEvent;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceUniqueName) {
        this.serviceName = serviceUniqueName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public SpanEvent getSpanEvent() {
        return spanEvent;
    }

    @Override
    public void setSpanEvent(SpanEvent spanEvent) {
        this.spanEvent = spanEvent;
    }
}
