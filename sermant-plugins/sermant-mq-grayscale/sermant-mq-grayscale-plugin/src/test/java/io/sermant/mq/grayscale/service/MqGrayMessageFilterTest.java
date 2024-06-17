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

import io.sermant.mq.grayscale.AbstactMqGrayTest;
import io.sermant.mq.grayscale.ConfigContextUtils;
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;

import org.apache.rocketmq.client.hook.FilterMessageContext;
import org.apache.rocketmq.common.message.MessageExt;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MqGrayMessageFilter test
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqGrayMessageFilterTest extends AbstactMqGrayTest {
    @Test
    public void testFilterMessage() {
        ConfigContextUtils.createMqGrayConfig(null);
        List<MessageExt> msgList = buildMessageList();
        FilterMessageContext context = new FilterMessageContext();
        context.setMsgList(msgList);
        MqGrayMessageFilter filter = new MqGrayMessageFilter();
        filter.filterMessage(context);
        Assert.assertEquals(1, msgList.size());

        List<MessageExt> msgList2 = buildMessageList();
        Map<String, Object> map = new HashMap<>();
        map.put("base.messageFilter.consumeType", "base");
        map.put("grayscale.environmentMatch.exact", new ArrayList<>());
        ConfigContextUtils.createMqGrayConfig(map);
        context.setMsgList(msgList2);
        filter.filterMessage(context);
        Assert.assertEquals(1, msgList2.size());
    }

    private List<MessageExt> buildMessageList() {
        List<MessageExt> msgList = new ArrayList<>();
        MessageExt messageExt1 = new MessageExt();
        messageExt1.putUserProperty(MqGrayscaleConfigUtils.MICRO_SERVICE_GRAY_TAG_KEY, "test%gray");

        MessageExt messageExt2 = new MessageExt();
        messageExt2.putUserProperty("tag", "junit%red");
        msgList.add(messageExt1);
        msgList.add(messageExt2);
        return msgList;
    }
}
