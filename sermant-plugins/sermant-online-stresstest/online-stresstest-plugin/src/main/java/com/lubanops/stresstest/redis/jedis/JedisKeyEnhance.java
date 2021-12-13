package com.lubanops.stresstest.redis.jedis;

import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Jedis 增强
 *
 * @author yiwei
 * @since 2021/11/1
 */
public class JedisKeyEnhance implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "redis.clients.jedis.Connection";
    private static final String INTERCEPT_CLASS = "com.lubanops.stresstest.redis.jedis.JedisKeyInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.named("sendCommand"))
        };
    }
}
