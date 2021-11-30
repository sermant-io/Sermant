package com.lubanops.stresstest.redis.jedis;

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
public class JedisPoolEnhance implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "redis.clients.jedis.JedisPool";
    private static final String INTERCEPT_CLASS = "com.lubanops.stresstest.redis.jedis.JedisPoolInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.named("getResource"))
        };
    }
}
