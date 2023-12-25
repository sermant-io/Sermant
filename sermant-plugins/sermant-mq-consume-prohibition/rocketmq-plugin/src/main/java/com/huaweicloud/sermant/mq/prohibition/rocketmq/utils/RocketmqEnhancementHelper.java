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
 * rocketmq拦截点辅助类
 *
 * @author daizhenyu
 * @since 2023-12-13
 **/
public class RocketmqEnhancementHelper {
    private static final String ENHANCE_PUSH_CONSUMER_CLASS =
            "org.apache.rocketmq.client.consumer.DefaultMQPushConsumer";

    private static final String ENHANCE_PULL_CONSUMER_CLASS =
            "org.apache.rocketmq.client.consumer.DefaultLitePullConsumer";

    private static final String START_METHOD_NAME = "start";

    private static final String SHUTDOWN_METHOD_NAME = "shutdown";

    private static final String SUBSCRIBE_METHOD_NAME = "subscribe";

    private static final String UNSUBSCRIBE_METHOD_NAME = "unsubscribe";

    private static final String ASSIGN_METHOD_NAME = "assign";

    private RocketmqEnhancementHelper() {
    }

    /**
     * 获取pushconsumer拦截点的ClassMatcher
     *
     * @return 返回ClassMatcher
     */
    public static ClassMatcher getPushConsumerClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_PUSH_CONSUMER_CLASS);
    }

    /**
     * 获取pullconsumer拦截点的ClassMatcher
     *
     * @return 返回ClassMatcher
     */
    public static ClassMatcher getPullConsumerClassMatcher() {
        return ClassMatcher.nameEquals(ENHANCE_PULL_CONSUMER_CLASS);
    }

    /**
     * 获取pushconsumer拦截点的start方法拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPushConsumerStartInterceptDeclarers() {
        return InterceptDeclarer.build(getStartMethodMatcher(), new RocketMqPushConsumerStartInterceptor());
    }

    /**
     * 获取带有RocketMqConsumerHandler的start方法拦截声明器
     *
     * @param handler rocketmq消费者外部扩展处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPushConsumerStartInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getStartMethodMatcher(), new RocketMqPushConsumerStartInterceptor(handler));
    }

    /**
     * 获取pullconsumer拦截点的start方法拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPullConsumerStartInterceptDeclarers() {
        return InterceptDeclarer.build(getStartMethodMatcher(), new RocketMqPullConsumerStartInterceptor());
    }

    /**
     * 获取带有RocketMqConsumerHandler的start方法拦截声明器
     *
     * @param handler rocketmq消费者外部扩展处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPullConsumerStartInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getStartMethodMatcher(), new RocketMqPullConsumerStartInterceptor(handler));
    }

    /**
     * 获取pushconsumer拦截点的shutdown方法拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPushConsumerShutdownInterceptDeclarers() {
        return InterceptDeclarer.build(getShutdownMethodMatcher(), new RocketMqPushConsumerShutdownInterceptor());
    }

    /**
     * 获取带有RocketMqConsumerHandler的shutdown方法拦截声明器
     *
     * @param handler rocketmq消费者外部扩展处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPushConsumerShutdownInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getShutdownMethodMatcher(),
                new RocketMqPushConsumerShutdownInterceptor(handler));
    }

    /**
     * 获取pullconsumer拦截点的shutdown方法拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPullConsumerShutdownInterceptDeclarers() {
        return InterceptDeclarer.build(getShutdownMethodMatcher(), new RocketMqPullConsumerShutdownInterceptor());
    }

    /**
     * 获取带有RocketMqConsumerHandler的shutdown方法拦截声明器
     *
     * @param handler rocketmq消费者外部扩展处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPullConsumerShutdownInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getShutdownMethodMatcher(),
                new RocketMqPullConsumerShutdownInterceptor(handler));
    }

    /**
     * 获取pushconsumer拦截点的subscribe方法拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPushConsumerSubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getSubscribeMethodMatcher(), new RocketMqPushConsumerSubscribeInterceptor());
    }

    /**
     * 获取带有RocketMqConsumerHandler的subscribe方法拦截声明器
     *
     * @param handler rocketmq消费者外部扩展处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPushConsumerSubscribeInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getSubscribeMethodMatcher(),
                new RocketMqPushConsumerSubscribeInterceptor(handler));
    }

    /**
     * 获取pullconsumer拦截点的subscribe方法拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPullConsumerSubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getSubscribeMethodMatcher(), new RocketMqPullConsumerSubscribeInterceptor());
    }

    /**
     * 获取带有RocketMqConsumerHandler的subscribe方法拦截声明器
     *
     * @param handler rocketmq消费者外部扩展处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPullConsumerSubscribeInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getSubscribeMethodMatcher(),
                new RocketMqPullConsumerSubscribeInterceptor(handler));
    }

    /**
     * 获取pushconsumer拦截点的unsubscribe方法拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPushConsumerUnsubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getUnsubscribeMethodMatcher(), new RocketMqPushConsumerUnsubscribeInterceptor());
    }

    /**
     * 获取带有RocketMqConsumerHandler的unsubscribe方法拦截声明器
     *
     * @param handler rocketmq消费者外部扩展处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPushConsumerUnsubscribeInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getUnsubscribeMethodMatcher(),
                new RocketMqPushConsumerUnsubscribeInterceptor(handler));
    }

    /**
     * 获取pullconsumer拦截点的unsubscribe方法拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPullConsumerUnsubscribeInterceptDeclarers() {
        return InterceptDeclarer.build(getUnsubscribeMethodMatcher(), new RocketMqPullConsumerUnsubscribeInterceptor());
    }

    /**
     * 获取带有RocketMqConsumerHandler的unsubscribe方法拦截声明器
     *
     * @param handler rocketmq消费者外部扩展处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPullConsumerUnsubscribeInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getUnsubscribeMethodMatcher(),
                new RocketMqPullConsumerUnsubscribeInterceptor(handler));
    }

    /**
     * 获取pullconsumer拦截点的assign方法拦截声明器
     *
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPullConsumerAssignInterceptDeclarers() {
        return InterceptDeclarer.build(getAssignMethodMatcher(), new RocketMqPullConsumerAssignInterceptor());
    }

    /**
     * 获取带有RocketMqConsumerHandler的assign方法拦截声明器
     *
     * @param handler rocketmq消费者外部扩展处理器
     * @return 返回拦截声明器
     */
    public static InterceptDeclarer getPullConsumerAssignInterceptDeclarers(RocketMqConsumerHandler handler) {
        return InterceptDeclarer.build(getAssignMethodMatcher(),
                new RocketMqPullConsumerAssignInterceptor(handler));
    }

    /**
     * 获取start方法拦截的方法匹配器
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getStartMethodMatcher() {
        return MethodMatcher.nameEquals(START_METHOD_NAME);
    }

    /**
     * 获取shutdown方法拦截的方法匹配器
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getShutdownMethodMatcher() {
        return MethodMatcher.nameEquals(SHUTDOWN_METHOD_NAME);
    }

    /**
     * 获取subscribe方法拦截的方法匹配器
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getSubscribeMethodMatcher() {
        return MethodMatcher.nameEquals(SUBSCRIBE_METHOD_NAME);
    }

    /**
     * 获取unsubscribe方法拦截的方法匹配器
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getUnsubscribeMethodMatcher() {
        return MethodMatcher.nameEquals(UNSUBSCRIBE_METHOD_NAME);
    }

    /**
     * 获取assign方法拦截的方法匹配器
     *
     * @return 方法匹配器
     */
    private static MethodMatcher getAssignMethodMatcher() {
        return MethodMatcher.nameEquals(ASSIGN_METHOD_NAME);
    }
}
