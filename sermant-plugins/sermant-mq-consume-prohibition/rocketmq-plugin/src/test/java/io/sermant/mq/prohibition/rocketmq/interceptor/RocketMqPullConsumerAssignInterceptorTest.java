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

package io.sermant.mq.prohibition.rocketmq.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.mq.prohibition.controller.rocketmq.RocketMqPullConsumerController;
import io.sermant.mq.prohibition.controller.utils.InvokeUtils;
import io.sermant.mq.prohibition.rocketmq.utils.PullConsumerLocalInfoUtils;

import org.apache.rocketmq.common.message.MessageQueue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * PullConsumer assign method interceptor UT
 *
 * @author daizhenyu
 * @since 2023-12-25
 **/
public class RocketMqPullConsumerAssignInterceptorTest extends BasePullConsumerInterceptorTest {
    private RocketMqPullConsumerAssignInterceptor interceptor = new RocketMqPullConsumerAssignInterceptor();

    private Collection<MessageQueue> messageQueues;

    private Set<String> topics;

    @Before
    public void setUp() {
        MockedStatic<InvokeUtils> invokeUtilsMockedStatic = Mockito.mockStatic(InvokeUtils.class);
        invokeUtilsMockedStatic
                .when(() -> InvokeUtils.isRocketMqInvokeBySermant(Thread.currentThread().getStackTrace()))
                .thenReturn(false);
        messageQueues = new ArrayList<>();
        MessageQueue messageQueue = new MessageQueue("test-topic", "broker-1", 1);
        messageQueues.add(messageQueue);
        topics = new HashSet<>();
        topics.add("test-topic");
    }

    @Test
    public void testAfter() {
        ExecuteContext context = ExecuteContext.forMemberMethod(pullConsumer, null, new Object[]{messageQueues},
                null, null);

        // wrapper为null
        interceptor.after(context);
        Assert.assertEquals(PullConsumerLocalInfoUtils.getSubscriptionType().name(), "ASSIGN");
        Assert.assertEquals(PullConsumerLocalInfoUtils.getMessageQueue(), messageQueues);
        PullConsumerLocalInfoUtils.removeSubscriptionType();
        PullConsumerLocalInfoUtils.removeMessageQueue();

        // Wrapper is null, message queue is null
        context = ExecuteContext.forMemberMethod(pullConsumer, null, new Object[]{null},
                null, null);
        interceptor.after(context);
        Assert.assertEquals(PullConsumerLocalInfoUtils.getSubscriptionType().name(), "NONE");
        PullConsumerLocalInfoUtils.removeSubscriptionType();

        // Wrapper is not null
        context = ExecuteContext.forMemberMethod(pullConsumer, null, new Object[]{messageQueues},
                null, null);
        RocketMqPullConsumerController.cachePullConsumer(pullConsumer);
        interceptor.after(context);
        Assert.assertEquals(pullConsumerWrapper.getSubscriptionType().name(), "ASSIGN");
        Assert.assertEquals(pullConsumerWrapper.getMessageQueues(), messageQueues);
        Assert.assertEquals(pullConsumerWrapper.getSubscribedTopics(), topics);
    }

    @After
    public void tearDown() {
        Mockito.clearAllCaches();
        RocketMqPullConsumerController.removePullConsumer(pullConsumer);
    }
}
