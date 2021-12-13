package com.lubanops.stresstest.redis.redisson.workaround;

import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Redisson Queue 增强
 *
 * @author yiwei
 * @since 2021/11/3
 */
public class RedissonQueueEnhance implements EnhanceDefinition {
    private static final String INTERCEPT_CLASS = "com.lubanops.stresstest.redis.redisson.workaround.RedissonShadowInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.multiClass("org.redisson.RedissonBoundedBlockingQueue",
                "org.redisson.RedissonBlockingQueue", "org.redisson.RedissonBlockingDeque");
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.named("trySetCapacity"))
        };
    }
}
