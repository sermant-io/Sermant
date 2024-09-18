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
import io.sermant.mq.grayscale.rocketmq.RocketMqTestAbstract;

import org.apache.rocketmq.client.impl.producer.DefaultMQProducerImpl;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.junit.Assert;
import org.junit.Test;

/**
 * RocketMqProducerGrayMessageHookInterceptor test
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class RocketMqProducerGrayMessageHookInterceptorTest extends RocketMqTestAbstract {
    @Test
    public void testDoAfter() throws Exception {
        RocketMqProducerGrayMessageHookInterceptor interceptor = new RocketMqProducerGrayMessageHookInterceptor();
        DefaultMQProducer producer = new DefaultMQProducer();
        DefaultMQProducerImpl producerImpl = new DefaultMQProducerImpl(producer);
        ExecuteContext context = ExecuteContext.forMemberMethod(producerImpl, null, null, null, null);
        interceptor.after(context);
        Assert.assertTrue(producerImpl.hasSendMessageHook());
    }
}
