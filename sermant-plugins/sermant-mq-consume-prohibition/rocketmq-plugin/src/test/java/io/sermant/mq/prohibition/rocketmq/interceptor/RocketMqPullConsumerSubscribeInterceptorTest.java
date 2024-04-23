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
import io.sermant.mq.prohibition.rocketmq.utils.PullConsumerLocalInfoUtils;

import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PullConsumer subscribe method interceptor UT
 *
 * @author daizhenyu
 * @since 2023-12-25
 **/
public class RocketMqPullConsumerSubscribeInterceptorTest extends BasePullConsumerInterceptorTest {
    private RocketMqPullConsumerSubscribeInterceptor interceptor = new RocketMqPullConsumerSubscribeInterceptor();

    private ConcurrentMap<String, SubscriptionData> subscription;

    private ExecuteContext context;

    @Before
    public void setUp() {
        subscription = new ConcurrentHashMap<String, SubscriptionData>();
        context = ExecuteContext.forMemberMethod(pullConsumer, null, null,
                null, null);
    }

    @Test
    public void testAfter() {
        // Wrapper is null
        interceptor.after(context);
        Assert.assertEquals(PullConsumerLocalInfoUtils.getSubscriptionType().name(), "SUBSCRIBE");
        PullConsumerLocalInfoUtils.removeSubscriptionType();

        // Wrapper is not null
        pullConsumerWrapper.setSubscribedTopics(subscription.keySet());
        subscription.put("test-topic", new SubscriptionData());
        RocketMqPullConsumerController.cachePullConsumer(pullConsumer);
        interceptor.after(context);
        Assert.assertEquals(pullConsumerWrapper.getSubscriptionType().name(), "SUBSCRIBE");
        Assert.assertEquals(pullConsumerWrapper.getSubscribedTopics(), subscription.keySet());
    }

    @After
    public void tearDown() {
        Mockito.clearAllCaches();
        RocketMqPullConsumerController.removePullConsumer(pullConsumer);
    }
}
