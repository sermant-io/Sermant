package com.lubanops.apm.bootstrap.trace;

public interface SampleFilter {

    boolean sample(String source, String httpMethod);

}
