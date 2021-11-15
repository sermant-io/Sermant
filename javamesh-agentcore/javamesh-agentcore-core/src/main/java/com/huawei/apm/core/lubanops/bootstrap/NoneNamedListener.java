package com.huawei.apm.core.lubanops.bootstrap;

import com.huawei.apm.core.agent.definition.TopListener;

import java.util.List;

public interface NoneNamedListener extends TopListener {

    void init();

    List<String> matchClass(String className, byte[] classfileBuffer);

    String getInterceptor();

    boolean hasAttribute();

}
