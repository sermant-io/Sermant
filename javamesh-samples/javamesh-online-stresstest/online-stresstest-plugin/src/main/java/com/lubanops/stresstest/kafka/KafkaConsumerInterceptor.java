/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */
package com.lubanops.stresstest.kafka;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
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
        return new ShadowConsumer((KafkaConsumer)result);
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}
