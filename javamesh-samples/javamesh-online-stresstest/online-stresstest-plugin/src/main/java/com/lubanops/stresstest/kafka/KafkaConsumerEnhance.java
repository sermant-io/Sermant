/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.kafka;


import com.huawei.apm.bootstrap.definition.EnhanceDefinition;
import com.huawei.apm.bootstrap.definition.MethodInterceptPoint;
import com.huawei.apm.bootstrap.matcher.ClassMatcher;
import com.huawei.apm.bootstrap.matcher.ClassMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.matcher.MethodReturnTypeMatcher;

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
                ElementMatchers.named("createRawConsumer"))
        };
    }
}
