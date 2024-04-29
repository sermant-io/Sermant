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
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

/**
 * PullConsumerLocalInfoUtils tool class unit testing
 *
 * @author daizhenyu
 * @since 2023-12-25
 **/
public class PullConsumerLocalInfoUtilsTest {
    @Test
    public void testSubscriptionTypeThreadLocal() {
        PullConsumerLocalInfoUtils.setSubscriptionType(SubscriptionType.ASSIGN);
        Assert.assertEquals(PullConsumerLocalInfoUtils.getSubscriptionType().name(), "ASSIGN");

        PullConsumerLocalInfoUtils.removeSubscriptionType();
        Assert.assertEquals(PullConsumerLocalInfoUtils.getSubscriptionType().name(), "NONE");

        PullConsumerLocalInfoUtils.removeSubscriptionType();
    }

    @Test
    public void testMessageQueueThreadLocal() {
        Collection<MessageQueue> messageQueues = new ArrayList<>();
        MessageQueue messageQueue = new MessageQueue();
        messageQueues.add(messageQueue);

        PullConsumerLocalInfoUtils.setMessageQueue(messageQueues);
        Assert.assertEquals(PullConsumerLocalInfoUtils.getMessageQueue().size(), 1);

        PullConsumerLocalInfoUtils.removeMessageQueue();
        Assert.assertEquals(PullConsumerLocalInfoUtils.getMessageQueue().size(), 0);

        PullConsumerLocalInfoUtils.removeSubscriptionType();
    }
}
