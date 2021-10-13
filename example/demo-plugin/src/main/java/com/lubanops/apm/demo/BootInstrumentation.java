package com.lubanops.apm.demo;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import com.huawei.net.bytebuddy.matcher.ElementMatchers;


public class BootInstrumentation implements EnhanceDefinition {
    public static final String ENHANCE_ANNOTATION = "org.springframework.boot.autoconfigure.SpringBootApplication";
    private static final String INTERCEPT_CLASS = "com.demo.BootInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.annotationWith(ENHANCE_ANNOTATION);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newStaticMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.named("main"))
        };
    }
}
