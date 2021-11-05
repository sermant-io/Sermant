package com.lubanops.stresstest.redis.jedis;

import com.huawei.apm.core.agent.definition.EnhanceDefinition;
import com.huawei.apm.core.agent.definition.MethodInterceptPoint;
import com.huawei.apm.core.agent.matcher.ClassMatcher;
import com.huawei.apm.core.agent.matcher.ClassMatchers;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Jedis cluster CRC 增强
 *
 * @author yiwei
 * @since 2021/11/1
 */
public class JedisClusterCRCEnhance implements EnhanceDefinition {
    private static final String OLD_ENHANCE_CLASS = "redis.clients.util.JedisClusterCRC16";
    private static final String NEW_ENHANCE_CLASS = "redis.clients.jedis.util.JedisClusterCRC16";
    private static final String INTERCEPT_CLASS = "com.lubanops.stresstest.redis.jedis.JedisClusterInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.multiClass(OLD_ENHANCE_CLASS, NEW_ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newStaticMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.named("getSlot"))
        };
    }
}
