package com.lubanops.stresstest.redis.lettuce;

import com.huawei.javamesh.core.agent.definition.EnhanceDefinition;
import com.huawei.javamesh.core.agent.definition.MethodInterceptPoint;
import com.huawei.javamesh.core.agent.matcher.ClassMatcher;
import com.huawei.javamesh.core.agent.matcher.ClassMatchers;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Jedis pool增强
 *
 * @author yiwei
 * @since 2021/11/3
 */
public class LettucePoolEnhance implements EnhanceDefinition {
    /**
     * ConnectionPoolSupport 方法 createGenericObjectPool
     */
    public static final String GENERIC_OBJECT_POOL_METHOD = "createGenericObjectPool";
    /**
     * ConnectionPoolSupport 方法 createSoftReferenceObjectPool
     */
    public static final String SOFT_OBJECT_POOL_METHOD = "createSoftReferenceObjectPool";
    private static final String ENHANCE_CLASS = "io.lettuce.core.support.ConnectionPoolSupport";
    private static final String INTERCEPT_CLASS = "com.lubanops.stresstest.redis.lettuce.LettucePoolInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newStaticMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.namedOneOf(GENERIC_OBJECT_POOL_METHOD, SOFT_OBJECT_POOL_METHOD))
        };
    }
}
