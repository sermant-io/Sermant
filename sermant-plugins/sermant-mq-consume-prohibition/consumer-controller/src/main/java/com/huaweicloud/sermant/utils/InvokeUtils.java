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

package com.huaweicloud.sermant.utils;

/**
 * 是否是Sermant发起的调用判断类
 *
 * @author lilai
 * @since 2023-12-18
 */
public class InvokeUtils {
    private static final String KAFKA_CONSUMER_CLASS_NAME = "org.apache.kafka.clients.consumer.KafkaConsumer";

    private static final String KAFKA_CONSUMER_CONTROLLER_CLASS_NAME = "com.huaweicloud.sermant.kafka.controller"
            + ".KafkaConsumerController";

    private static final String ROCKETMQ_PULL_CONSUMER_CLASS_NAME =
            "org.apache.rocketmq.client.consumer.DefaultLitePullConsumer";

    private static final String ROCKETMQ_CONSUMER_CONTROLLER_CLASS_NAME =
            "com.huaweicloud.sermant.rocketmq.controller.RocketMqPullConsumerController";

    private InvokeUtils() {
    }

    /**
     * kafka禁消费判断是否Sermant发起的调用
     *
     * @param stackTrace 当前线程调用栈
     * @return 是否Sermant发起的调用
     */
    public static boolean isKafkaInvokeBySermant(StackTraceElement[] stackTrace) {
        return isInvokeBySermant(KAFKA_CONSUMER_CLASS_NAME, KAFKA_CONSUMER_CONTROLLER_CLASS_NAME, stackTrace);
    }

    /**
     * rocketmq禁消费判断是否Sermant发起的调用
     *
     * @param stackTrace 当前线程调用栈
     * @return 是否Sermant发起的调用
     */
    public static boolean isRocketMqInvokeBySermant(StackTraceElement[] stackTrace) {
        return isInvokeBySermant(ROCKETMQ_PULL_CONSUMER_CLASS_NAME, ROCKETMQ_CONSUMER_CONTROLLER_CLASS_NAME,
                stackTrace);
    }

    private static boolean isInvokeBySermant(String consumerClassName, String consumerControllerClassName,
            StackTraceElement[] stackTrace) {
        int stackTraceIdxMax = stackTrace.length - 1;
        for (int i = 0; i < stackTrace.length; i++) {
            if (!consumerClassName.equals(stackTrace[i].getClassName())) {
                continue;
            }
            if (i == stackTraceIdxMax) {
                break;
            }
            if (consumerClassName.equals(stackTrace[i + 1].getClassName())) {
                continue;
            }
            return consumerControllerClassName.equals(stackTrace[i + 1].getClassName());
        }
        return false;
    }
}
