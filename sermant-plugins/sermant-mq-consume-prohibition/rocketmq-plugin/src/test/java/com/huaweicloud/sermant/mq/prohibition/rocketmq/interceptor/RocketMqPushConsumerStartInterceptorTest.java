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
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPushConsumerController;

import org.apache.rocketmq.client.exception.MQClientException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * PushConsumer start method interceptor UT
 *
 * @author daizhenyu
 * @since 2023-12-25
 **/
public class RocketMqPushConsumerStartInterceptorTest extends BasePushConsumerInterceptorTest {
    private RocketMqPushConsumerStartInterceptor interceptor = new RocketMqPushConsumerStartInterceptor();

    private ExecuteContext context;

    @Before
    public void setUp() throws MQClientException {
        context = ExecuteContext.forMemberMethod(pushConsumer, null, null,
                null, null);
        pushConsumer.subscribe("test-topic", "*");
    }

    @Test
    public void testAfter() {
        interceptor.after(context);
        Assert.assertTrue(pushConsumerWrapper.getSubscribedTopics().contains("test-topic"));
    }

    @After
    public void tearDown() {
        Mockito.clearAllCaches();
        RocketMqPushConsumerController.removePushConsumer(pushConsumer);
    }
}
