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

package io.sermant.mq.prohibition.rocketmq.utils;

import io.sermant.mq.prohibition.controller.rocketmq.constant.SubscriptionType;

import org.apache.rocketmq.common.message.MessageQueue;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Pull consumer's thread variable
 *
 * @author daizhenyu
 * @since 2023-12-20
 **/
public class PullConsumerLocalInfoUtils {
    private static ThreadLocal<SubscriptionType> subscriptionTypeThreadLocal =
            new ThreadLocal<>();

    private static ThreadLocal<Collection<MessageQueue>> messageQueueThreadLocal =
            new ThreadLocal<>();

    private PullConsumerLocalInfoUtils() {
    }

    /**
     * Get consumer's subscription type
     *
     * @return subscription type
     */
    public static SubscriptionType getSubscriptionType() {
        SubscriptionType subscriptionType = subscriptionTypeThreadLocal.get();
        if (subscriptionType == null) {
            subscriptionType = SubscriptionType.NONE;
            subscriptionTypeThreadLocal.set(subscriptionType);
        }
        return subscriptionType;
    }

    /**
     * Set consumer subscription type
     *
     * @param subscriptionType subscription type
     */
    public static void setSubscriptionType(SubscriptionType subscriptionType) {
        subscriptionTypeThreadLocal.set(subscriptionType);
    }

    /**
     * Remove consumer's subscription type
     */
    public static void removeSubscriptionType() {
        subscriptionTypeThreadLocal.remove();
    }

    /**
     * Obtain consumer assigned consumption queue
     *
     * @return Consumer assigned consumption queue
     */
    public static Collection<MessageQueue> getMessageQueue() {
        Collection<MessageQueue> messageQueues = messageQueueThreadLocal.get();
        if (messageQueues == null) {
            messageQueues = new ArrayList<>();
            messageQueueThreadLocal.set(messageQueues);
        }
        return messageQueues;
    }

    /**
     * Set up consumption queues for consumer assignment
     *
     * @param messageQueues Consumer assigned consumption queue
     */
    public static void setMessageQueue(Collection<MessageQueue> messageQueues) {
        messageQueueThreadLocal.set(messageQueues);
    }

    /**
     * Remove consumer assigned consumption queue
     */
    public static void removeMessageQueue() {
        messageQueueThreadLocal.remove();
    }
}
