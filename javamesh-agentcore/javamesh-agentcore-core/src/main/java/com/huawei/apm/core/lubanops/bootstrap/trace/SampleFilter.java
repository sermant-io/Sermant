package com.huawei.apm.core.lubanops.bootstrap.trace;

public interface SampleFilter {

    boolean sample(String source, String httpMethod);

}
