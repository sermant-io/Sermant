package com.huawei.route.server.conditions;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 开启nacos同步开关
 * 仅当配置url时才开启
 *
 * @author zhouss
 * @since 2021-10-08
 */
public class NacosSyncCondition implements Condition {
    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String selector = conditionContext.getEnvironment().getProperty("route.server.gray.register-type");
        return "nacos".equals(selector);
    }
}
