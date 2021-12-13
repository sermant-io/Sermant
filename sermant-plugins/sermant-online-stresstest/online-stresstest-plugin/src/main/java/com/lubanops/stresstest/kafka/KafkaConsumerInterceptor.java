/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
 */
package com.lubanops.stresstest.kafka;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.lang.reflect.Method;

/**
 * KafkaConsumer 增强实现
 *
 * @author yiwei
 * @since 2021/10/27
 */
public class KafkaConsumerInterceptor implements InstanceMethodInterceptor {
    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        if (result instanceof ShadowConsumer) {
            return result;
        }
        return new ShadowConsumer<>((KafkaConsumer<?, ?>)result);
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}
