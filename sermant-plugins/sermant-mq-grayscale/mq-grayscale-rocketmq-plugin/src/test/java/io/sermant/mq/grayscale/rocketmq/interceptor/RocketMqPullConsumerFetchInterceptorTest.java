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

import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPullConsumerImpl;
import org.apache.rocketmq.client.impl.consumer.RebalanceImpl;
import org.apache.rocketmq.client.impl.consumer.RebalancePullImpl;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * RocketMqPullConsumerFetchInterceptor test
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class RocketMqPullConsumerFetchInterceptorTest extends RocketMqTestAbstract {
    @Test
    public void testDoAfter() throws Exception {
        DefaultMQPullConsumer consumer = new DefaultMQPullConsumer("consumerGroup");
        DefaultMQPullConsumerImpl pullConsumer = new DefaultMQPullConsumerImpl(consumer, null);
        consumer.setNamesrvAddr("127.0.0.1:9876");
        Object[] arguments = new Object[]{"TOPIC_TEST"};
        RocketMqPullConsumerFetchInterceptor interceptor = new RocketMqPullConsumerFetchInterceptor();
        RebalanceImpl rebalanced = buildRebalanceImpl(pullConsumer);

        // gray instance
        ExecuteContext context = ExecuteContext.forMemberMethod(pullConsumer, null, arguments, null, null);
        interceptor.doAfter(context);
        Assert.assertTrue(RocketMqSubscriptionDataUtils.getGrayTagChangeFlag("TOPIC_TEST", rebalanced));

        // base instance
        Map<String, Object> map = new HashMap<>();
        map.put("x_lane_tag", "red");
        GrayConfigContextUtils.createMqGrayConfig(map, DynamicConfigEventType.CREATE);
        context = ExecuteContext.forMemberMethod(pullConsumer, null, arguments, null, null);
        interceptor.doAfter(context);
        Assert.assertTrue(RocketMqSubscriptionDataUtils.getGrayTagChangeFlag("TOPIC_TEST", rebalanced));
    }

    private RebalanceImpl buildRebalanceImpl(DefaultMQPullConsumerImpl pullConsumer) {
        RebalanceImpl rebalanced = new RebalancePullImpl(pullConsumer);
        setRebalancedImplInfo(rebalanced);
        return rebalanced;
    }
}
