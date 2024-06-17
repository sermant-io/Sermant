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

package io.sermant.mq.grayscale.service;

import io.sermant.core.utils.tag.TrafficTag;
import io.sermant.core.utils.tag.TrafficUtils;
import io.sermant.mq.grayscale.AbstactMqGrayTest;
import io.sermant.mq.grayscale.ConfigContextUtils;
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;

import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.common.message.Message;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MqGraySendMessageHook test
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqGraySendMessageHookTest extends AbstactMqGrayTest {
    @Test
    public void testSendMessageBefore() {
        ConfigContextUtils.createMqGrayConfig(null);
        SendMessageContext context = new SendMessageContext();
        Message message = new Message();
        context.setMessage(message);
        MqGraySendMessageHook messageHook = new MqGraySendMessageHook();
        messageHook.sendMessageBefore(context);
        Assert.assertEquals("test%gray", message.getProperty(MqGrayscaleConfigUtils.MICRO_SERVICE_GRAY_TAG_KEY));

        Map<String, Object> map = new HashMap<>();
        map.put("grayscale.environmentMatch.exact", new ArrayList<>());
        ConfigContextUtils.createMqGrayConfig(map);
        ConfigContextUtils.setTrafficTag();
        messageHook.sendMessageBefore(context);
        Assert.assertEquals("test%gray", message.getProperty(MqGrayscaleConfigUtils.MICRO_TRAFFIC_GRAY_TAG_KEY));
        TrafficUtils.removeTrafficTag();
    }
}
