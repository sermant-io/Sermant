package com.huawei.flowrecord.plugins.http.v4;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;
import net.bytebuddy.matcher.ElementMatchers;

public class HttpServerInstrumentation implements EnhanceDefinition {

    private static final String ENHANCE_CLASS = "org.springframework.web.servlet.DispatcherServlet";
    private static final String INTERCEPT_CLASS = "com.huawei.flowrecord.plugins.http.v4.HttpServerInterceptor";


    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
                MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS, ElementMatchers.named("doService"))
        };
    }
}
