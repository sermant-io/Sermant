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

import io.sermant.mq.grayscale.rocketmq.utils.RocketMqGrayscaleConfigUtils;

import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.common.message.Message;

/**
 * gray message send hook service
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class RocketMqGraySendMessageHook implements SendMessageHook {
    @Override
    public String hookName() {
        return "MqGraySendMessageHook";
    }

    @Override
    public void sendMessageBefore(SendMessageContext context) {
        Message message = context.getMessage();

        // set traffic tags in message by matching serviceMeta
        RocketMqGrayscaleConfigUtils.injectTrafficTagByServiceMeta(message);
    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {
    }
}
