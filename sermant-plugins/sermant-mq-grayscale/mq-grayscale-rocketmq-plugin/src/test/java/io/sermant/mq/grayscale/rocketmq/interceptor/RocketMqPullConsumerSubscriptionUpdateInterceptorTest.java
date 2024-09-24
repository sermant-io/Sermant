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

package io.sermant.mq.grayscale.rocketmq.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.mq.grayscale.rocketmq.RocketMqTestAbstract;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPullConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.junit.Assert;
import org.junit.Test;

/**
 * RocketMqPullConsumerSubscriptionUpdateInterceptor test
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class RocketMqPullConsumerSubscriptionUpdateInterceptorTest extends RocketMqTestAbstract {
    @Test
    public void testDoAfter() throws Exception {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("consumerGroup");
        DefaultMQPullConsumerImpl pullConsumer = new DefaultMQPullConsumerImpl(consumer, null);
        consumer.setNamesrvAddr("127.0.0.1:9876");
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setNamesrvAddr("127.0.0.1:9876");
        MQClientInstance instance = new MQClientInstance(clientConfig, 0, "1");
        ReflectUtils.setFieldValue(pullConsumer, "mQClientFactory", instance);
        ExecuteContext context = ExecuteContext.forMemberMethod(pullConsumer, null, null, null, null);
        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setTopic("TOPIC_TEST");
        context.afterMethod(subscriptionData, null);
        RocketMqPullConsumerSubscriptionUpdateInterceptor interceptor
                = new RocketMqPullConsumerSubscriptionUpdateInterceptor();
        interceptor.doAfter(context);
        Assert.assertEquals("(x_lane_canary in ('gray'))", subscriptionData.getSubString());
    }
}
