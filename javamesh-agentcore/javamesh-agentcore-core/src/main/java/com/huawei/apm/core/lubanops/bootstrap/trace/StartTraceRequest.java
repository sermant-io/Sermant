package com.huawei.apm.core.lubanops.bootstrap.trace;

public class StartTraceRequest {
    private String className;

    private String method;

    private String kind;

    private String source;

    private String realSource;

    private String httpMethod;

    private String traceId;

    private String spanId;

    private String sourceEventId;

    private String domainId;

    private SampleFilter sampleFilter;

    private String gTraceId;

    public StartTraceRequest(String className, String method, String traceId, String spanId, String gTraceId) {
        this.className = className;
        this.method = method;
        this.traceId = traceId;
        this.spanId = spanId;
        this.gTraceId = gTraceId;
    }

    public String getMethod() {
        return method;
    }

    public String getTraceClass() {
        return className;
    }

    public String getKind() {
        return kind;
    }

    public String getSource() {
        return source;
    }

    public String getRealSource() {
        return realSource;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public String getDomainId() {
        return domainId;
    }

    public SampleFilter getSampleFilter() {
        return sampleFilter;
    }

    public String getgTraceId() {
        return gTraceId;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setRealSource(String realSource) {
        this.realSource = realSource;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public void setSourceEventId(String sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public void setSampleFilter(SampleFilter sampleFilter) {
        this.sampleFilter = sampleFilter;
    }

    public void setgTraceId(String gTraceId) {
        this.gTraceId = gTraceId;
    }
}
