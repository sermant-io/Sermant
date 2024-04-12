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

package com.huaweicloud.sermant.mq.prohibition.rocketmq.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPullConsumerController;

import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PullConsumer unsubscribe method interceptor UT
 *
 * @author daizhenyu
 * @since 2023-12-25
 **/
public class RocketMqPullConsumerUnsubscribeInterceptorTest extends BasePullConsumerInterceptorTest {
    private RocketMqPullConsumerUnsubscribeInterceptor interceptor = new RocketMqPullConsumerUnsubscribeInterceptor();

    private ConcurrentMap<String, SubscriptionData> subscription;

    private ExecuteContext context;

    @Before
    public void setUp() {
        subscription = new ConcurrentHashMap<String, SubscriptionData>();
        subscription.put("test-topic", new SubscriptionData());
        pullConsumerWrapper.setSubscribedTopics(subscription.keySet());
        RocketMqPullConsumerController.cachePullConsumer(pullConsumer);
        context = ExecuteContext.forMemberMethod(pullConsumer, null, null,
                null, null);
    }

    @Test
    public void testAfter() {
        subscription.remove("test-topic");
        interceptor.after(context);
        Assert.assertEquals(pullConsumerWrapper.getSubscribedTopics().size(), 0);
    }

    @After
    public void tearDown() {
        Mockito.clearAllCaches();
        RocketMqPullConsumerController.removePullConsumer(pullConsumer);
    }
}
