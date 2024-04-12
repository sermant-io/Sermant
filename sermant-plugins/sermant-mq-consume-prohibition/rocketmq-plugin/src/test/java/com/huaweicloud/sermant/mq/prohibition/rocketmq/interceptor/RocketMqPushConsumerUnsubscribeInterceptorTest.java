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
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPushConsumerController;

import org.apache.rocketmq.client.exception.MQClientException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * PushConsumer unsubscribe method interceptor UT
 *
 * @author daizhenyu
 * @since 2023-12-25
 **/
public class RocketMqPushConsumerUnsubscribeInterceptorTest extends BasePushConsumerInterceptorTest {
    private RocketMqPushConsumerUnsubscribeInterceptor interceptor = new RocketMqPushConsumerUnsubscribeInterceptor();

    private ExecuteContext context;

    @Before
    public void setUp() throws MQClientException {
        context = ExecuteContext.forMemberMethod(pushConsumer, null, null,
                null, null);
        pushConsumer.subscribe("test-topic", "*");
        pushConsumerWrapper
                .setSubscribedTopics(pushConsumer.getDefaultMQPushConsumerImpl().getSubscriptionInner().keySet());
        RocketMqPushConsumerController.cachePushConsumer(pushConsumer);
    }

    @Test
    public void testAfter() {
        pushConsumer.unsubscribe("test-topic");
        interceptor.after(context);
        Assert.assertFalse(pushConsumerWrapper.getSubscribedTopics().contains("test-topic"));
    }

    @After
    public void tearDown() {
        Mockito.clearAllCaches();
        RocketMqPushConsumerController.removePushConsumer(pushConsumer);
    }
}
