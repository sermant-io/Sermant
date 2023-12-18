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
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerAssignInterceptor;
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerCloseInterceptor;
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerMapConstructorInterceptor;
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerPollInterceptor;
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerPropertiesConstructorInterceptor;
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerSubscribeInterceptor;
import com.huaweicloud.sermant.kafka.interceptor.KafkaConsumerUnSubscribeInterceptor;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.serialization.Deserializer;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Kafka拦截点辅助类
 *
 * @author lilai
 * @since 2023-12-05
 */
public class KafkaEnhancementHelper {
    private static final String ENHANCE_CLASS = "org.apache.kafka.clients.consumer.KafkaConsumer";

    private static final int SUBSCRIBE_PARAM_COUNT = 2;

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
     * 获取Kafka properties构造方法拦截点的拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPropertiesConstructorInterceptDeclarers() {
        return InterceptDeclarer.build(getPropertiesConstructorMethodMatcher(),
                new KafkaConsumerPropertiesConstructorInterceptor());
    }

    /**
     * 获取带有KafkaConsumerHandler的Kafka properties构造方法拦截声明器
     *
     * @param handler Kafka消费者处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPropertiesConstructorInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getPropertiesConstructorMethodMatcher(),
                new KafkaConsumerPropertiesConstructorInterceptor(handler));
    }

    /**
     * 获取Kafka map构造方法拦截点的拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getMapConstructorInterceptDeclarers() {
        return InterceptDeclarer.build(getMapConstructorMethodMatcher(),
                new KafkaConsumerMapConstructorInterceptor());
    }

    /**
     * 获取带有KafkaConsumerHandler的Kafka map构造方法拦截声明器
     *
     * @param handler Kafka消费者处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getMapConstructorInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getMapConstructorMethodMatcher(),
                new KafkaConsumerMapConstructorInterceptor(handler));
    }

    /**
     * 获取Kafka subscribe方法拦截点的拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getSubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getSubScribeMethodMatcher(), new KafkaConsumerSubscribeInterceptor());
    }

    /**
     * 获取带有KafkaConsumerHandler的Kafka subscribe方法拦截点的拦截声明器
     *
     * @param handler 处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getSubscribeInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getSubScribeMethodMatcher(), new KafkaConsumerSubscribeInterceptor(handler));
    }

    /**
     * 获取Kafka assign方法拦截点的拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getAssignInterceptDeclarers() {
        return InterceptDeclarer.build(getAssignMethodMatcher(), new KafkaConsumerAssignInterceptor());
    }

    /**
     * 获取带有KafkaConsumerHandler的Kafka assign方法拦截点的拦截声明器
     *
     * @param handler 处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getAssignInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getAssignMethodMatcher(), new KafkaConsumerAssignInterceptor(handler));
    }

    /**
     * 获取Kafka unsubscribe方法拦截点的拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getUnsubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getUnSubscribeMethodMatcher(), new KafkaConsumerUnSubscribeInterceptor());
    }

    /**
     * 获取带有KafkaConsumerHandler的Kafka unsubscribe方法拦截点的拦截声明器
     *
     * @param handler 处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getUnsubscribeInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getUnSubscribeMethodMatcher(), new KafkaConsumerUnSubscribeInterceptor(handler));
    }

    /**
     * 获取Kafka close方法拦截点的拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getCloseInterceptDeclarers() {
        return InterceptDeclarer.build(getCloseMethodMatcher(), new KafkaConsumerCloseInterceptor());
    }

    /**
     * 获取带有KafkaConsumerHandler的Kafka close方法拦截点的拦截声明器
     *
     * @param handler 处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getCloseInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getCloseMethodMatcher(), new KafkaConsumerCloseInterceptor(handler));
    }

    /**
     * 获取Kafka poll方法拦截点的拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPollInterceptDeclarers() {
        return InterceptDeclarer.build(getPollMethodMatcher(), new KafkaConsumerPollInterceptor());
    }

    /**
     * 获取带有KafkaConsumerHandler的Kafka poll方法拦截点的拦截声明器
     *
     * @param handler 处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPollInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getPollMethodMatcher(), new KafkaConsumerPollInterceptor(handler));
    }

    /**
     * 获取Properties构造方法拦截的方法匹配器
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#KafkaConsumer(Properties, Deserializer, Deserializer)}
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getPropertiesConstructorMethodMatcher() {
        return MethodMatcher.isConstructor().and(MethodMatcher.paramTypesEqual(Properties.class,
                Deserializer.class, Deserializer.class));
    }

    /**
     * 获取Map构造方法拦截的方法匹配器
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#KafkaConsumer(Map, Deserializer, Deserializer)}
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getMapConstructorMethodMatcher() {
        return MethodMatcher.isConstructor()
                .and(MethodMatcher.paramTypesEqual(Map.class, Deserializer.class, Deserializer.class));
    }

    /**
     * 获取subscribe方法拦截的方法匹配器
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#subscribe(Pattern, ConsumerRebalanceListener)}
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#subscribe(Collection, ConsumerRebalanceListener)}
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getSubScribeMethodMatcher() {
        return MethodMatcher.nameEquals("subscribe").and(MethodMatcher.paramCountEquals(SUBSCRIBE_PARAM_COUNT));
    }

    /**
     * 获取assign方法拦截的方法匹配器
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#assign(Collection)}
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getAssignMethodMatcher() {
        return MethodMatcher.nameEquals("assign");
    }

    /**
     * 获取unsubscribe方法拦截的方法匹配器
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#unsubscribe()}
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getUnSubscribeMethodMatcher() {
        return MethodMatcher.nameEquals("unsubscribe");
    }

    /**
     * 获取Poll方法拦截的方法匹配器
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#poll(long)}
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#poll(Duration)}
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getPollMethodMatcher() {
        return MethodMatcher.nameEquals("poll");
    }

    /**
     * 获取Close方法拦截的方法匹配器
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#close(long, TimeUnit)}
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getCloseMethodMatcher() {
        return MethodMatcher.nameEquals("close").and(MethodMatcher.paramTypesEqual(Long.TYPE, TimeUnit.class));
    }
}
