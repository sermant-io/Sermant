package com.huawei.flowcontrol;

import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * dubbo增强
 * apache dubbo
 * alibaba dubbo
 */
public abstract class DubboDefinition implements EnhanceDefinition {
    private final String enhanceClass;

    private final String interceptorClass;

    protected DubboDefinition(String enhanceClass, String interceptorClass) {
        this.enhanceClass = enhanceClass;
        this.interceptorClass = interceptorClass;
    }

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(enhanceClass);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{
            MethodInterceptPoint.newInstMethodInterceptPoint(interceptorClass,
                ElementMatchers.<MethodDescription>named("invoke"))};
    }
}
