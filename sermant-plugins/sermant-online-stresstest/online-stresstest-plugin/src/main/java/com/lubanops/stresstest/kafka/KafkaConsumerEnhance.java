/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */

package com.lubanops.stresstest.kafka;


import com.huawei.sermant.core.agent.definition.EnhanceDefinition;
import com.huawei.sermant.core.agent.definition.MethodInterceptPoint;
import com.huawei.sermant.core.agent.matcher.ClassMatcher;
import com.huawei.sermant.core.agent.matcher.ClassMatchers;

import net.bytebuddy.matcher.ElementMatchers;

/**
 * KafkaConsumer 增强
 *
 * @author yiwei
 * @since 2021/10/26
 */
public class KafkaConsumerEnhance implements EnhanceDefinition {
    private static final String ENHANCE_CLASS = "org.springframework.kafka.core.DefaultKafkaConsumerFactory";
    private static final String INTERCEPT_CLASS = "com.lubanops.stresstest.kafka.KafkaConsumerInterceptor";

    @Override
    public ClassMatcher enhanceClass() {
        return ClassMatchers.named(ENHANCE_CLASS);
    }

    @Override
    public MethodInterceptPoint[] getMethodInterceptPoints() {
        return new MethodInterceptPoint[]{MethodInterceptPoint.newInstMethodInterceptPoint(INTERCEPT_CLASS,
                ElementMatchers.named("createConsumer"))
        };
    }
}
