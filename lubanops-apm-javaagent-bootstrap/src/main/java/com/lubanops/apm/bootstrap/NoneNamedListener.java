package com.lubanops.apm.bootstrap;

import com.huawei.apm.bootstrap.definition.TopListener;

import java.util.List;

public interface NoneNamedListener extends TopListener {

    void init();

    List<String> matchClass(String className, byte[] classfileBuffer);

    String getInterceptor();

    boolean hasAttribute();

}
