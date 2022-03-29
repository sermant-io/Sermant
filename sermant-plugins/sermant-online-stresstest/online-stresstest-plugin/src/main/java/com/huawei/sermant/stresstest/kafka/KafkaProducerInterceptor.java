/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.sermant.stresstest.kafka;

import static com.huawei.sermant.stresstest.config.Constant.TEST_FLAG;
import static com.huawei.sermant.stresstest.config.Constant.TEST_VALUE;
import static com.huawei.sermant.stresstest.core.Reflection.addPrefixOnDeclaredField;

import com.huawei.sermant.core.agent.common.BeforeResult;
import com.huawei.sermant.core.agent.interceptor.InstanceMethodInterceptor;
import com.huawei.sermant.stresstest.config.ConfigFactory;
import com.huawei.sermant.stresstest.core.Tester;

import org.apache.kafka.clients.producer.ProducerRecord;

import java.lang.reflect.Method;

/**
 * KafkaProducer 增强实现
 *
 * @author yiwei
 * @since 2021-10-21
 */
@SuppressWarnings("checkstyle:RegexpSinglelineJava")
public class KafkaProducerInterceptor implements InstanceMethodInterceptor {
    private static final String TOPIC_FIELD = "topic";

    @Override
    public void before(Object obj, Method method, Object[] arguments, BeforeResult beforeResult) {
        if (!Tester.isTest()) {
            return;
        }
        if (arguments.length > 0 && arguments[0] instanceof ProducerRecord) {
            ((ProducerRecord<?, ?>)arguments[0]).headers().add(TEST_FLAG, TEST_VALUE.getBytes());
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
