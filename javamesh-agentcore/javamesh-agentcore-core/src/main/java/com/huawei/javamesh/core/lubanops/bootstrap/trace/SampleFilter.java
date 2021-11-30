package com.huawei.javamesh.core.lubanops.bootstrap.trace;

public interface SampleFilter {

    boolean sample(String source, String httpMethod);

}
