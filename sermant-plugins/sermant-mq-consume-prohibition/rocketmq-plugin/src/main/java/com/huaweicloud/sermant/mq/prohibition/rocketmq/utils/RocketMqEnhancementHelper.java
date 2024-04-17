/*
 *  Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.mq.prohibition.rocketmq.utils;

import com.huaweicloud.sermant.core.plugin.agent.declarer.InterceptDeclarer;
import com.huaweicloud.sermant.core.plugin.agent.matcher.ClassMatcher;
import com.huaweicloud.sermant.core.plugin.agent.matcher.MethodMatcher;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor.RocketMqPullConsumerAssignInterceptor;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor.RocketMqPullConsumerShutdownInterceptor;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor.RocketMqPullConsumerStartInterceptor;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor.RocketMqPullConsumerSubscribeInterceptor;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor.RocketMqPullConsumerUnsubscribeInterceptor;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor.RocketMqPushConsumerShutdownInterceptor;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor.RocketMqPushConsumerStartInterceptor;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor.RocketMqPushConsumerSubscribeInterceptor;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor.RocketMqPushConsumerUnsubscribeInterceptor;
import com.huaweicloud.sermant.rocketmq.extension.RocketMqConsumerHandler;

/**
 * Rocketmq interception point auxiliary class
 *
 * @author daizhenyu
 * @since 2023-12-13
 **/
public class RocketMqEnhancementHelper {
    private static final String ENHANCE_PUSH_CONSUMER_CLASS =
            "org.apache.rocketmq.client.consumer.DefaultMQPushConsumer";

    private static final String ENHANCE_PULL_CONSUMER_CLASS =
            "org.apache.rocketmq.client.consumer.DefaultLitePullConsumer";

    private static final String START_METHOD_NAME = "start";

    private static final String SHUTDOWN_METHOD_NAME = "shutdown";

    private static final String SUBSCRIBE_METHOD_NAME = "subscribe";

    private static final String UNSUBSCRIBE_METHOD_NAME = "unsubscribe";

    private static final String ASSIGN_METHOD_NAME = "assign";

    private RocketMqEnhancementHelper() {
    }

    /**
     * Obtain ClassMatcher for pushConsumer interception point
     *
     * @return Return classMatcher
     */
    public static ClassMatcher getPushConsumerClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_PUSH_CONSUMER_CLASS);
    }

    /**
     * Obtain ClassMatcher for pullConsumer interception point
     *
     * @return Return classMatcher
     */
    public static ClassMatcher getPullConsumerClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_PULL_CONSUMER_CLASS);
    }

    /**
     * Obtain the interception declarer for the start method of pushconsumer interception point
     *
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPushConsumerStartInterceptDeclarers() {
        return InterceptDeclarer.build(getStartMethodMatcher(), new RocketMqPushConsumerStartInterceptor());
    }

    /**
     * Obtain the interception declarer for the start method with RocketMqConsumerHandler
     *
     * @param handler rocketmq consumer external extension handler
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPushConsumerStartInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getStartMethodMatcher(), new RocketMqPushConsumerStartInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the start method of pullconsumer interception point
     *
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPullConsumerStartInterceptDeclarers() {
        return InterceptDeclarer.build(getStartMethodMatcher(), new RocketMqPullConsumerStartInterceptor());
    }

    /**
     * Obtain the interception declarer for the start method with RocketMqConsumerHandler
     *
     * @param handler rocketmq consumer external extension handler
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPullConsumerStartInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getStartMethodMatcher(), new RocketMqPullConsumerStartInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the shutdown method of pushconsumer interception point
     *
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPushConsumerShutdownInterceptDeclarers() {
        return InterceptDeclarer.build(getShutdownMethodMatcher(), new RocketMqPushConsumerShutdownInterceptor());
    }

    /**
     * Obtain the interception declarer for the shutdown method with RocketMqConsumerHandler
     *
     * @param handler rocketmq consumer external extension handler
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPushConsumerShutdownInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getShutdownMethodMatcher(),
                new RocketMqPushConsumerShutdownInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the shutdown method of pullconsumer interception point
     *
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPullConsumerShutdownInterceptDeclarers() {
        return InterceptDeclarer.build(getShutdownMethodMatcher(), new RocketMqPullConsumerShutdownInterceptor());
    }

    /**
     * Obtain the interception declarer for the shutdown method with RocketMqConsumerHandler
     *
     * @param handler rocketmq consumer external extension handler
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPullConsumerShutdownInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getShutdownMethodMatcher(),
                new RocketMqPullConsumerShutdownInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the subscribe method of pushconsumer interception point
     *
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPushConsumerSubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getSubscribeMethodMatcher(), new RocketMqPushConsumerSubscribeInterceptor());
    }

    /**
     * Obtain the interception declarer for the subscribe method with RocketMqConsumerHandler
     *
     * @param handler rocketmq consumer external extension handler
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPushConsumerSubscribeInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getSubscribeMethodMatcher(),
                new RocketMqPushConsumerSubscribeInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the subscribe method of pullconsumer interception point
     *
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPullConsumerSubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getSubscribeMethodMatcher(), new RocketMqPullConsumerSubscribeInterceptor());
    }

    /**
     * Obtain the interception declarer for the subscribe method with RocketMqConsumerHandler
     *
     * @param handler rocketmq consumer external extension handler
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPullConsumerSubscribeInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getSubscribeMethodMatcher(),
                new RocketMqPullConsumerSubscribeInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the unsubscribe method of pushconsumer interception point
     *
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPushConsumerUnsubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getUnsubscribeMethodMatcher(), new RocketMqPushConsumerUnsubscribeInterceptor());
    }

    /**
     * Obtain the interception declarer for the unsubscribe method with RocketMqConsumerHandler
     *
     * @param handler rocketmq consumer external extension handler
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPushConsumerUnsubscribeInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getUnsubscribeMethodMatcher(),
                new RocketMqPushConsumerUnsubscribeInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the unsubscribe method of pullconsumer interception point
     *
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPullConsumerUnsubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getUnsubscribeMethodMatcher(), new RocketMqPullConsumerUnsubscribeInterceptor());
    }

    /**
     * Obtain the interception declarer for the unsubscribe method with RocketMqConsumerHandler
     *
     * @param handler rocketmq consumer external extension handler
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPullConsumerUnsubscribeInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getUnsubscribeMethodMatcher(),
                new RocketMqPullConsumerUnsubscribeInterceptor(handler));
    }

    /**
     * Obtain the interception declarer for the assign method of pullconsumer interception point
     *
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPullConsumerAssignInterceptDeclarers() {
        return InterceptDeclarer.build(getAssignMethodMatcher(), new RocketMqPullConsumerAssignInterceptor());
    }

    /**
     * Obtain the interception declarer for the assign method with RocketMqConsumerHandler
     *
     * @param handler rocketmq consumer external extension handler
     * @return return to the interception declarer
     */
    public static InterceptDeclarer getPullConsumerAssignInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getAssignMethodMatcher(),
                new RocketMqPullConsumerAssignInterceptor(handler));
    }

    /**
     * Obtain the method matcher intercepted by the start method
     *
     * @return method matcher
     */
    private static MethodMatcher getStartMethodMatcher() {
        return MethodMatcher.nameEquals(START_METHOD_NAME);
    }

    /**
     * Obtain the method matcher intercepted by the shutdown method
     *
     * @return method matcher
     */
    private static MethodMatcher getShutdownMethodMatcher() {
        return MethodMatcher.nameEquals(SHUTDOWN_METHOD_NAME);
    }

    /**
     * Obtain the method matcher intercepted by the subscribe method
     *
     * @return method matcher
     */
    private static MethodMatcher getSubscribeMethodMatcher() {
        return MethodMatcher.nameEquals(SUBSCRIBE_METHOD_NAME);
    }

    /**
     * Obtain the method matcher intercepted by the unsubscribe method
     *
     * @return method matcher
     */
    private static MethodMatcher getUnsubscribeMethodMatcher() {
        return MethodMatcher.nameEquals(UNSUBSCRIBE_METHOD_NAME);
    }

    /**
     * Obtain the method matcher intercepted by the assign method
     *
     * @return method matcher
     */
    private static MethodMatcher getAssignMethodMatcher() {
        return MethodMatcher.nameEquals(ASSIGN_METHOD_NAME);
    }
}
