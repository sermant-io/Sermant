package com.lubanops.stresstest.redis.redisson;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Redisson key 增强
 *
 * @author yiwei
 * @since 2021/11/2
 */
public class RedissonKeyEnhance implements EnhanceDefinition {
    private static final String ENHANCE_ASYNC_CLASS = "org.redisson.command.CommandAsyncService";
    private static final String ENHANCE_BATCH_CLASS = "org.redisson.command.CommandBatchService";
    private static final String INTERCEPT_CLASS = "com.lubanops.stresstest.redis.redisson.RedissonKeyInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.multiClass(ENHANCE_ASYNC_CLASS, ENHANCE_BATCH_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.named("async"))
        };
    }
}
