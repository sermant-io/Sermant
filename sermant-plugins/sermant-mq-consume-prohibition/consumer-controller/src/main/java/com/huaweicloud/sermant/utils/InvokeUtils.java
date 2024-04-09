/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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
 * Call the judgment class to determine if it is a call initiated by Sermant
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
     * Kafka's prohibit on consumption determines whether the call was initiated by Sermant
     *
     * @param stackTrace The current thread call stack
     * @return Whether or not Sermant initiated the call
     */
    public static boolean isKafkaInvokeBySermant(StackTraceElement[] stackTrace) {
        return isInvokeBySermant(KAFKA_CONSUMER_CLASS_NAME, KAFKA_CONSUMER_CONTROLLER_CLASS_NAME, stackTrace);
    }

    /**
     * Rocketmq prohibits consumption and determines whether the call is initiated by Sermant
     *
     * @param stackTrace The current thread call stack
     * @return Whether or not Sermant initiated the call
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
