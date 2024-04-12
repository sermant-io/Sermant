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
 * Kafka interception point auxiliary class
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
     * Obtain ClassMatcher for Kafka interception point
     *
     * @return Return to ClassMatcher
     */
    public static ClassMatcher getClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_CLASS);
    }

    /**
     * Obtain the interception declarer for the Kafka properties construction method interception point
     *
     * @return Return interception declarer
     */
    public static InterceptDeclarer getPropertiesConstructorInterceptDeclarers() {
        return InterceptDeclarer.build(getPropertiesConstructorMethodMatcher(),
                new KafkaConsumerPropertiesConstructorInterceptor());
    }

    /**
     * Obtain the interception declarer for the Kafka properties construction method with KafkaConsumerHandler
     *
     * @param handler Kafka Consumer handler
     * @return Return interception declarer
     */
    public static InterceptDeclarer getPropertiesConstructorInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getPropertiesConstructorMethodMatcher(),
                new KafkaConsumerPropertiesConstructorInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the Kafka map construction method interception point
     *
     * @return Return interception declarer
     */
    public static InterceptDeclarer getMapConstructorInterceptDeclarers() {
        return InterceptDeclarer.build(getMapConstructorMethodMatcher(),
                new KafkaConsumerMapConstructorInterceptor());
    }

    /**
     * Obtain the interception declarer for the Kafka map construction method with KafkaConsumerHandler
     *
     * @param handler Kafka Consumer handler
     * @return Return interception declarer
     */
    public static InterceptDeclarer getMapConstructorInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getMapConstructorMethodMatcher(),
                new KafkaConsumerMapConstructorInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the Kafka subscribe method interception point
     *
     * @return Return interception declarer
     */
    public static InterceptDeclarer getSubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getSubScribeMethodMatcher(), new KafkaConsumerSubscribeInterceptor());
    }

    /**
     * Obtain the interception declarer for the Kafka subscribe method interception point with KafkaConsumerHandler
     *
     * @param handler handler
     * @return Return interception declarer
     */
    public static InterceptDeclarer getSubscribeInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getSubScribeMethodMatcher(), new KafkaConsumerSubscribeInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the Kafka assign method interception point
     *
     * @return Return interception declarer
     */
    public static InterceptDeclarer getAssignInterceptDeclarers() {
        return InterceptDeclarer.build(getAssignMethodMatcher(), new KafkaConsumerAssignInterceptor());
    }

    /**
     * Obtain the interception declarer for the Kafka assign method interception point with KafkaConsumerHandler
     *
     * @param handler handler
     * @return Return interception declarer
     */
    public static InterceptDeclarer getAssignInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getAssignMethodMatcher(), new KafkaConsumerAssignInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the Kafka unsubscribe method interception point
     *
     * @return Return interception declarer
     */
    public static InterceptDeclarer getUnsubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getUnSubscribeMethodMatcher(), new KafkaConsumerUnSubscribeInterceptor());
    }

    /**
     * Obtain the interception declarer for the Kafka unsubscribe method interception point with KafkaConsumerHandler
     *
     * @param handler handler
     * @return Return interception declarer
     */
    public static InterceptDeclarer getUnsubscribeInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getUnSubscribeMethodMatcher(), new KafkaConsumerUnSubscribeInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the Kafka close method interception point
     *
     * @return Return interception declarer
     */
    public static InterceptDeclarer getCloseInterceptDeclarers() {
        return InterceptDeclarer.build(getCloseMethodMatcher(), new KafkaConsumerCloseInterceptor());
    }

    /**
     * Obtain the interception declarer for the Kafka close method interception point with KafkaConsumerHandler
     *
     * @param handler handler
     * @return Return interception declarer
     */
    public static InterceptDeclarer getCloseInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getCloseMethodMatcher(), new KafkaConsumerCloseInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the Kafka poll method interception point
     *
     * @return Return interception declarer
     */
    public static InterceptDeclarer getPollInterceptDeclarers() {
        return InterceptDeclarer.build(getPollMethodMatcher(), new KafkaConsumerPollInterceptor());
    }

    /**
     * Obtain the interception declarer for the Kafka poll method interception point with KafkaConsumerHandler
     *
     * @param handler handler
     * @return Return interception declarer
     */
    public static InterceptDeclarer getPollInterceptDeclarers(KafkaConsumerHandler handler) {
        return InterceptDeclarer.build(getPollMethodMatcher(), new KafkaConsumerPollInterceptor(handler));
    }

    /**
     * Obtain the method matcher for intercepting properties construction methods
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#KafkaConsumer(Properties, Deserializer, Deserializer)}
     *
     * @return Method matcher
     */
    private static MethodMatcher getPropertiesConstructorMethodMatcher() {
        return MethodMatcher.isConstructor().and(MethodMatcher.paramTypesEqual(Properties.class,
                Deserializer.class, Deserializer.class));
    }

    /**
     * Obtain the method matcher for intercepting Map construction methods
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#KafkaConsumer(Map, Deserializer, Deserializer)}
     *
     * @return Method matcher
     */
    private static MethodMatcher getMapConstructorMethodMatcher() {
        return MethodMatcher.isConstructor()
                .and(MethodMatcher.paramTypesEqual(Map.class, Deserializer.class, Deserializer.class));
    }

    /**
     * Obtain the method matcher for intercepting subscribe method
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#subscribe(Pattern, ConsumerRebalanceListener)}
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#subscribe(Collection, ConsumerRebalanceListener)}
     *
     * @return Method matcher
     */
    private static MethodMatcher getSubScribeMethodMatcher() {
        return MethodMatcher.nameEquals("subscribe").and(MethodMatcher.paramCountEquals(SUBSCRIBE_PARAM_COUNT));
    }

    /**
     * Obtain the method matcher for intercepting assign method
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#assign(Collection)}
     *
     * @return Method matcher
     */
    private static MethodMatcher getAssignMethodMatcher() {
        return MethodMatcher.nameEquals("assign");
    }

    /**
     * Obtain the method matcher for intercepting unsubscribe method
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#unsubscribe()}
     *
     * @return Method matcher
     */
    private static MethodMatcher getUnSubscribeMethodMatcher() {
        return MethodMatcher.nameEquals("unsubscribe");
    }

    /**
     * Obtain the method matcher for intercepting Poll method
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#poll(long)}
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#poll(Duration)}
     *
     * @return Method matcher
     */
    private static MethodMatcher getPollMethodMatcher() {
        return MethodMatcher.nameEquals("poll");
    }

    /**
     * Obtain the method matcher for intercepting Close method
     * {@link org.apache.kafka.clients.consumer.KafkaConsumer#close(long, TimeUnit)}
     *
     * @return Method matcher
     */
    private static MethodMatcher getCloseMethodMatcher() {
        return MethodMatcher.nameEquals("close").and(MethodMatcher.paramTypesEqual(Long.TYPE, TimeUnit.class));
    }
}
