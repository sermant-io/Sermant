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

package io.sermant.mq.grayscale.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.mq.grayscale.AbstactMqGrayTest;
import io.sermant.mq.grayscale.ConfigContextUtils;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.junit.Assert;
import org.junit.Test;

/**
 * MqPullConsumerConstructorInterceptor test
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqPullConsumerConstructorInterceptorTest extends AbstactMqGrayTest {
    @Test
    public void testAfter() throws Exception {
        DefaultLitePullConsumer consumer = new DefaultLitePullConsumer("consumerGroup");
        ExecuteContext context = ExecuteContext.forMemberMethod(consumer, null, null, null, null);
        MqPullConsumerConstructorInterceptor interceptor = new MqPullConsumerConstructorInterceptor();
        ConfigContextUtils.createMqGrayConfig(null);
        interceptor.after(context);
        Assert.assertEquals(consumer.getConsumerGroup(), "consumerGroup_test%gray");
    }
}
