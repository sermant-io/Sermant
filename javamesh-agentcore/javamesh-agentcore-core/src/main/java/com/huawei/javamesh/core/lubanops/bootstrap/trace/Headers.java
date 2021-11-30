package com.huawei.javamesh.core.lubanops.bootstrap.trace;

/**
 * tracing header constance.
 */
public enum Headers {

    TRACE_ID("lubanops-ntrace-id"),
    GTRACE_ID("lubanops-gtrace-id"),
    SPAN_ID("lubanops-nspan-id"),
    ENV_ID("lubanops-nenv-id"),
    RESPONSE_ENV_ID("lubanops-response-nenv-id"),
    SOURCE_EVENT_ID("lubanops-sevent-id"),
    CSE_CONTEXT("x-cse-context"),
    DUBBO_CONTEXT("x-dubbo-context"),
    DOMAIN_ID("lubanops-ndomain-id");

    public static final String CLASS_FULL_NAME = Headers.class.getName();

    private String value;

    Headers(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
