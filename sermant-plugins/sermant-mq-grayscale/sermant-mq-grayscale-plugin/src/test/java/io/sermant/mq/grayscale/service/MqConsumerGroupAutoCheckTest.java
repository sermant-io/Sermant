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

package io.sermant.mq.grayscale.service;

import io.sermant.mq.grayscale.AbstactMqGrayTest;
import io.sermant.mq.grayscale.ConfigContextUtils;
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;

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

/**
 * MqConsumerGroupAutoCheck test
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqConsumerGroupAutoCheckTest extends AbstactMqGrayTest {
    @Test
    public void testSchedulerCheckGrayConsumerStart() throws Exception {
        ConfigContextUtils.createMqGrayConfig(null);
        MqConsumerGroupAutoCheck.setTopic("TEST-TOPIC");
        MqConsumerGroupAutoCheck.setOriginGroup("group");
        MQClientInstance instance = Mockito.mock(MQClientInstance.class);
        MqConsumerGroupAutoCheck.setMqClientInstance(instance);
        MQClientAPIImpl mqClientAPI = Mockito.mock(MQClientAPIImpl.class);
        Mockito.when(instance.getMQClientAPIImpl()).thenReturn(mqClientAPI);
        TopicRouteData topicRouteData = Mockito.mock(TopicRouteData.class);
        Mockito.when(mqClientAPI.getTopicRouteInfoFromNameServer("TEST-TOPIC", 5000L, false))
                .thenReturn(topicRouteData);

        HashMap<Long, String> brokerAddrs = new HashMap<>();
        brokerAddrs.put(123L, "127.0.0.1:9876");
        BrokerData brokerData = new BrokerData();
        brokerData.setBrokerAddrs(brokerAddrs);
        List<BrokerData> brokerDataList = new ArrayList<>();
        brokerDataList.add(brokerData);
        Mockito.when(topicRouteData.getBrokerDatas()).thenReturn(brokerDataList);

        GroupList groupList = new GroupList();
        HashSet<String> list = new HashSet<>();
        list.add("group_junit%red");
        groupList.setGroupList(list);
        Mockito.when(mqClientAPI.queryTopicConsumeByWho("127.0.0.1:9876", "TEST-TOPIC", 5000L))
                .thenReturn(groupList);

        List<String> consumerIds = new ArrayList<>();
        consumerIds.add("123");
        Mockito.when(mqClientAPI.getConsumerIdListByGroup("127.0.0.1:9876", "group_junit%red", 5000L))
                .thenReturn(consumerIds);

        MqConsumerGroupAutoCheck.schedulerCheckGrayConsumerStart();
        Assert.assertTrue(MqGrayscaleConfigUtils.isExcludeTagsContainsTag("junit%red"));
    }
}
