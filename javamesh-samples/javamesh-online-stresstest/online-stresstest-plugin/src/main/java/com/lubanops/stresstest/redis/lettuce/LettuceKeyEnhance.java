package com.lubanops.stresstest.redis.lettuce;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Lettuce key 增强
 *
 * @author yiwei
 * @since 2021/11/2
 */
public class LettuceKeyEnhance implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "io.lettuce.core.protocol.CommandArgs";
    private static final String INTERCEPT_CLASS = "com.lubanops.stresstest.redis.lettuce.LettuceKeyInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.namedOneOf("addKey", "addKeys"))
        };
    }
}
