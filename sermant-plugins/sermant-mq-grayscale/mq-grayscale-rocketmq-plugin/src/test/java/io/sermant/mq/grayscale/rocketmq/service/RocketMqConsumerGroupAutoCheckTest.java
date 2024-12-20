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

package io.sermant.mq.grayscale.rocketmq.service;

import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.mq.grayscale.rocketmq.RocketMqTestAbstract;
import io.sermant.mq.grayscale.rocketmq.GrayConfigContextUtils;
import io.sermant.mq.grayscale.rocketmq.utils.RocketMqSubscriptionDataUtils;

import org.apache.rocketmq.client.ClientConfig;
import org.apache.rocketmq.client.impl.MQClientAPIImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.protocol.body.GroupList;
import org.apache.rocketmq.common.protocol.route.BrokerData;
import org.apache.rocketmq.common.protocol.route.TopicRouteData;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * RocketMqConsumerGroupAutoCheck test
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class RocketMqConsumerGroupAutoCheckTest extends RocketMqTestAbstract {
    @Test
    public void testExpressionByAutoFindGrayGroup() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("x_lane_tag", "red");
        map.put("x_lane_canary", "red");
        GrayConfigContextUtils.createMqGrayConfig(map, DynamicConfigEventType.CREATE);
        RocketMqConsumerGroupAutoCheck.setConsumerClientConfig("127.0.0.1:9876", "TEST-TOPIC", "group_junit");

        ClientConfig clientConfig = Mockito.mock(ClientConfig.class);
        Mockito.when(clientConfig.getNamesrvAddr()).thenReturn("127.0.0.1:9876");

        MQClientInstance instance = Mockito.mock(MQClientInstance.class);
        Mockito.when(instance.getClientConfig()).thenReturn(clientConfig);

        MQClientAPIImpl mqClientAPI = Mockito.mock(MQClientAPIImpl.class);
        Mockito.when(instance.getMQClientAPIImpl()).thenReturn(mqClientAPI);

        GroupList groupList = new GroupList();
        HashSet<String> list = new HashSet<>();
        list.add("group_junit_gray");
        groupList.setGroupList(list);

        List<String> consumerIds = new ArrayList<>();
        consumerIds.add("123");
        Mockito.when(mqClientAPI.getConsumerIdListByGroup("127.0.0.1:9876", "group_junit_gray", 5000L))
                .thenReturn(consumerIds);
        String subscribeScope = RocketMqSubscriptionDataUtils.buildSubscribeScope("TEST-TOPIC",
                "group_junit", "127.0.0.1:9876");

        RocketMqConsumerGroupAutoCheck.setMqClientInstance("TEST-TOPIC", "group_junit", instance);
        String expression = RocketMqSubscriptionDataUtils
                .addGrayTagsToSql92Expression("x_lane_canary in ('red')", subscribeScope);
        Assert.assertEquals("(x_lane_canary not in ('red')) or (x_lane_canary is null)", expression);
    }
}
