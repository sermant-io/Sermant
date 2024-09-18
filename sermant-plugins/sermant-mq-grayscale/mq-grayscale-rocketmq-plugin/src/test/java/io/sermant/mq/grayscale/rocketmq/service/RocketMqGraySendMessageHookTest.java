/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.mq.grayscale.rocketmq.service;

import io.sermant.mq.grayscale.rocketmq.RocketMqTestAbstract;

import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.common.message.Message;
import org.junit.Assert;
import org.junit.Test;

/**
 * RocketMqGraySendMessageHook test
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class RocketMqGraySendMessageHookTest extends RocketMqTestAbstract {
    @Test
    public void testSchedulerCheckGrayConsumerStart() {
        RocketMqGraySendMessageHook messageHook = new RocketMqGraySendMessageHook();

        // message has no x_lane_canary property
        Message message = new Message();
        SendMessageContext context = new SendMessageContext();
        context.setMessage(message);
        messageHook.sendMessageBefore(context);
        Assert.assertEquals("gray", message.getProperty("x_lane_canary"));

        // message has x_lane_canary property
        Message message1 = new Message();
        message1.putUserProperty("x_lane_canary", "red");
        context.setMessage(message1);
        Assert.assertEquals("red", message1.getProperty("x_lane_canary"));
    }
}
