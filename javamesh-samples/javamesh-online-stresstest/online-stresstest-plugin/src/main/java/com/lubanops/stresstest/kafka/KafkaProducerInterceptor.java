/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.lubanops.stresstest.kafka;

import com.huawei.apm.bootstrap.common.BeforeResult;
import com.huawei.apm.bootstrap.interceptors.InstanceMethodInterceptor;
import com.lubanops.stresstest.config.ConfigFactory;
import com.lubanops.stresstest.core.Tester;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.lang.reflect.Method;

import static com.lubanops.stresstest.core.Reflection.*;
import static com.lubanops.stresstest.core.Tester.TEST_FLAG;
import static com.lubanops.stresstest.core.Tester.TEST_VALUE;

/**
 * KafkaProducer 增强实现
 *
 * @author yiwei
 * @since 2021/10/21
 */
public class KafkaProducerInterceptor implements InstanceMethodInterceptor {
    private static final String TOPIC_FIELD = "topic";

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (!Tester.isTest()) {
            return;
        }
        if (arguments.length > 0 && arguments[0] instanceof ProducerRecord) {
            ((ProducerRecord<?, ?>) arguments[0]).headers().add(TEST_FLAG, TEST_VALUE.getBytes());
            addPrefixOnDeclaredField(TOPIC_FIELD, arguments[0], ConfigFactory.getConfig().getTestTopicPrefix());
        }
    }

    @Override
    public Object after(Object obj, Method method, Object[] arguments, Object result) {
        return result;
    }

    @Override
    public void onThrow(Object obj, Method method, Object[] arguments, Throwable throwable) {
    }
}
