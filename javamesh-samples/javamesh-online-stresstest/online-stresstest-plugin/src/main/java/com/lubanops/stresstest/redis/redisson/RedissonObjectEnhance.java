package com.lubanops.stresstest.redis.redisson;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Redisson Object 增强
 *
 * @author yiwei
 * @since 2021/11/3
 */
public class RedissonObjectEnhance implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "org.redisson.command.RedisExecutor";
    private static final String INTERCEPT_CLASS = "com.lubanops.stresstest.redis.redisson.RedissonObjectInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newConstructorInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.any())
        };
    }
}
