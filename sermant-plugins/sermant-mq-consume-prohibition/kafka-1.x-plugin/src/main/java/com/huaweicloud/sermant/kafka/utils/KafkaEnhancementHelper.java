/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.kafka.utils;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.kafka.extension.KafkaConsumerHandler;
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerConstructorInterceptor;
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerPollInterceptor;

/**
 * Kafka拦截点辅助类
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaEnhancementHelper {
    private static final String ENHANCE_CLASS = "org.apache.kafka.clients.consumer.KafkaConsumer";

    private static final int PARAM_COUNT = 3;

    private KafkaEnhancementHelper() {
    }

    /**
     * 获取Kafka拦截点的ClassMatcher
     *
     * @return 返回ClassMatcher
     */
    public static ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    /**
     * 获取Kafka拦截点的拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getConstructorInterceptDeclarers() {
        return InterceptDeclarer.build(getConstructorMethodMatcher(), new KafkaConsumerConstructorInterceptor());
    }

    /**
     * 获取带有KafkaConsumerHandler的拦截声明器
     *
     * @param handler Kafka消费者处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getConstructorInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getConstructorMethodMatcher(), new KafkaConsumerConstructorInterceptor(handler));
    }

    /**
     * 获取构造方法拦截的方法匹配器
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getConstructorMethodMatcher() {
        return MethodMatcher.isConstructor().and(MethodMatcher.paramCountEquals(PARAM_COUNT));
    }

    /**
     * 获取Poll方法拦截的方法匹配器
     *
     * @return 方法匹配器
     */
    public static InterceptDeclarer getPollInterceptDeclarers() {
        return InterceptDeclarer.build(getPollMethodMatcher(), new KafkaConsumerPollInterceptor());
    }

    private static MethodMatcher getPollMethodMatcher() {
        return MethodMatcher.nameEquals("poll");
    }
}
