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
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.mq.grayscale.rocketmq.RocketMqTestAbstract;
import io.sermant.mq.grayscale.rocketmq.GrayConfigContextUtils;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * RocketMqConsumerConstructorInterceptor test
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class RocketMqConsumerConstructorInterceptorTest extends RocketMqTestAbstract {
    @Test
    public void testBefore() throws Exception {
        DefaultLitePullConsumer consumer = new DefaultLitePullConsumer("consumerGroup");
        Object[] arguments = new Object[]{"TOPIC_TEST", "consumerGroup"};
        ExecuteContext context = ExecuteContext.forMemberMethod(consumer, null, arguments, null, null);
        RocketMqConsumerConstructorInterceptor interceptor = new RocketMqConsumerConstructorInterceptor();
        interceptor.before(context);
        Assert.assertEquals(arguments[1], "consumerGroup_gray");

        // serviceMeta not match, not set group with tag
        Map<String, Object> map = new HashMap<>();
        map.put("x_lane_tag", "red");
        GrayConfigContextUtils.createMqGrayConfig(map, DynamicConfigEventType.CREATE);
        arguments[1] = "consumerGroup";
        context = ExecuteContext.forMemberMethod(consumer, null, arguments, null, null);
        interceptor.before(context);
        Assert.assertEquals(arguments[1], "consumerGroup");
    }
}
