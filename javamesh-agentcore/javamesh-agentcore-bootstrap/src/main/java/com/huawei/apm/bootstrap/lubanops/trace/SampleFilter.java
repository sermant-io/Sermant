package com.huawei.apm.bootstrap.lubanops.trace;

public interface SampleFilter {

    boolean sample(String source, String httpMethod);

}
