package com.lubanops.stresstest.redis.redisson;

import com.huawei.javamesh.core.agent.definition.EnhanceDefinition;
import com.huawei.javamesh.core.agent.definition.MethodInterceptPoint;
import com.huawei.javamesh.core.agent.matcher.ClassMatcher;
import com.huawei.javamesh.core.agent.matcher.ClassMatchers;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Redisson cluster 增强
 *
 * @author yiwei
 * @since 2021/11/2
 */
public class RedissonClusterEnhance implements EnhanceDefinition {
    private static final String CLUSTER_ENHANCE_CLASS = "org.redisson.cluster.ClusterConnectionManager";
    private static final String MASTER_SLAVE_ENHANCE_CLASS = "org.redisson.connection.MasterSlaveConnectionManager";
    private static final String INTERCEPT_CLASS = "com.lubanops.stresstest.redis.redisson.RedissonClusterInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.multiClass(CLUSTER_ENHANCE_CLASS, MASTER_SLAVE_ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.named("calcSlot"))
        };
    }
}
