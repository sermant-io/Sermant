/*
 *  Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.mq.prohibition.rocketmq.utils.PullConsumerLocalInfoUtils;
import com.huaweicloud.sermant.rocketmq.constant.SubscriptionType;
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPullConsumerController;

import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PullConsumer start method interceptor UT
 *
 * @author daizhenyu
 * @since 2023-12-25
 **/
public class RocketMqPullConsumerStartInterceptorTest extends BasePullConsumerInterceptorTest {
    private RocketMqPullConsumerStartInterceptor interceptor = new RocketMqPullConsumerStartInterceptor();

    private ExecuteContext context;

    private Collection<MessageQueue> messageQueues;

    @Before
    public void setUp() {
        context = ExecuteContext.forMemberMethod(pullConsumer, null, null,
                null, null);

        messageQueues = new ArrayList<>();
        MessageQueue messageQueue = new MessageQueue();
        messageQueues.add(messageQueue);

        ConcurrentHashMap<String, SubscriptionData> topics = new ConcurrentHashMap<>();
        topics.put("test-topic", new SubscriptionData());
        Mockito.when(pullConsumerWrapper.getRebalanceImpl().getSubscriptionInner()).thenReturn(topics);
    }

    @Test
    public void testAfter() {
        // the subscription method is assign
        PullConsumerLocalInfoUtils.setSubscriptionType(SubscriptionType.ASSIGN);
        PullConsumerLocalInfoUtils.setMessageQueue(messageQueues);
        interceptor.after(context);
        Assert.assertEquals(pullConsumerWrapper.getSubscriptionType().name(), "ASSIGN");
        Assert.assertEquals(pullConsumerWrapper.getMessageQueues(), messageQueues);
        PullConsumerLocalInfoUtils.removeMessageQueue();
        PullConsumerLocalInfoUtils.removeSubscriptionType();

        // subscription method is SUBSCRIBE
        PullConsumerLocalInfoUtils.setSubscriptionType(SubscriptionType.SUBSCRIBE);
        interceptor.after(context);
        Assert.assertTrue(pullConsumerWrapper.getSubscribedTopics().contains("test-topic"));
        Assert.assertEquals(pullConsumerWrapper.getSubscriptionType().name(), "SUBSCRIBE");
        PullConsumerLocalInfoUtils.removeSubscriptionType();
    }

    @After
    public void tearDown() {
        Mockito.clearAllCaches();
        RocketMqPullConsumerController.removePullConsumer(pullConsumer);
    }
}
