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
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqSubscriptionDataUtils;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultLitePullConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.client.impl.consumer.RebalanceLitePullImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * RocketMqLitePullConsumerSubscribeInterceptor test
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class RocketMqLitePullConsumerSubscribeInterceptorTest extends RocketMqTestAbstract {
    @Test
    public void testDoAfter() throws Exception {
        DefaultLitePullConsumer consumer = new DefaultLitePullConsumer("consumerGroup");
        DefaultLitePullConsumerImpl litePullConsumer = new DefaultLitePullConsumerImpl(consumer, null);
        consumer.setNamesrvAddr("127.0.0.1:9876");
        Object[] arguments = new Object[]{"TOPIC_TEST"};
        RebalanceImpl rebalanced = buildRebalanceImpl(litePullConsumer);
        RocketMqLitePullConsumerSubscribeInterceptor interceptor = new RocketMqLitePullConsumerSubscribeInterceptor();

        // gray instance
        ExecuteContext context = ExecuteContext.forMemberMethod(litePullConsumer, null, arguments, null, null);
        interceptor.doAfter(context);
        Assert.assertTrue(RocketMqSubscriptionDataUtils.getGrayTagChangeFlag("TOPIC_TEST", rebalanced));

        // base instance
        Map<String, Object> map = new HashMap<>();
        map.put("x_lane_tag", "red");
        GrayConfigContextUtils.createMqGrayConfig(map, DynamicConfigEventType.CREATE);
        context = ExecuteContext.forMemberMethod(litePullConsumer, null, arguments, null, null);
        interceptor.doAfter(context);
        Assert.assertTrue(RocketMqSubscriptionDataUtils.getGrayTagChangeFlag("TOPIC_TEST", rebalanced));
    }

    private RebalanceImpl buildRebalanceImpl(DefaultLitePullConsumerImpl litePullConsumer) {
        RebalanceImpl rebalanced = new RebalanceLitePullImpl(litePullConsumer);
        setRebalancedImplInfo(rebalanced);
        return rebalanced;
    }
}
