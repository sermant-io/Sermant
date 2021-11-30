package com.lubanops.apm.plugin.threadlocal;

import com.huawei.javamesh.core.agent.definition.EnhanceDefinition;
import com.huawei.javamesh.core.agent.definition.MethodInterceptPoint;
import com.huawei.javamesh.core.agent.matcher.ClassMatcher;
import com.huawei.javamesh.core.agent.matcher.ClassMatchers;
import com.lubanops.apm.plugin.common.Constant;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * ScheduledThreadpool 增强
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class ScheduledThreadPoolEnhance implements EnhanceDefinition, Constant {
    /**
     * 增强类的全限定名
     */
    private static final String ENHANCE_CLASS = "java.util.concurrent.ScheduledThreadPoolExecutor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.namedOneOf("schedule", "scheduleAtFixedRate","scheduleWithFixedDelay",
                        "submit", "execute"))};
    }
}
