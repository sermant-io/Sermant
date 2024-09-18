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

package io.sermant.mq.grayscale.rocketmq.utils;

import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.mq.grayscale.config.ConsumeModeEnum;
import io.sermant.mq.grayscale.config.GrayTagItem;
import io.sermant.mq.grayscale.rocketmq.RocketMqTestAbstract;
import io.sermant.mq.grayscale.rocketmq.GrayConfigContextUtils;
import io.sermant.mq.grayscale.rocketmq.config.RocketMqConsumerClientConfig;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.common.filter.ExpressionType;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RocketMqSubscriptionDataUtils test
 *
 * @author chengyouling
 * @since 2024-09-10
 **/
public class RocketMqSubscriptionDataUtilsTest extends RocketMqTestAbstract {
    @Test
    public void testBuildSql92ExpressionByTags() {
        Set<String> tagsSet = new HashSet<>();
        tagsSet.add("gray");
        String expression = RocketMqSubscriptionDataUtils.buildSql92ExpressionByTags(tagsSet);
        Assert.assertEquals("(TAGS is not null and TAGS in ('gray'))", expression);
    }

    @Test
    public void testAddGrayTagsToSql92Expression() {
        createConfig();
        // test gray group
        String key = RocketMqSubscriptionDataUtils.buildSubscribeScope("TEST_TOPIC", "group",
                "127.0.0.1:9876");
        String expression = RocketMqSubscriptionDataUtils.addGrayTagsToSql92Expression("", key);
        Assert.assertEquals("(x_lane_canary in ('gray'))", expression);

        // test auto model
        Map<String, Object> map = new HashMap<>();
        map.put("x_lane_tag", "red");
        map.put("x_lane_canary", "red");
        GrayConfigContextUtils.createMqGrayConfig(map, DynamicConfigEventType.CREATE);
        expression = RocketMqSubscriptionDataUtils
                .addGrayTagsToSql92Expression("x_lane_canary in ('red')", key);
        Assert.assertEquals("(x_lane_canary not in ('red')) or (x_lane_canary is null)", expression);

        // test base model
        map.put("consumeMode", ConsumeModeEnum.BASE);
        GrayConfigContextUtils.createMqGrayConfig(map, DynamicConfigEventType.CREATE);
        expression = RocketMqSubscriptionDataUtils.addGrayTagsToSql92Expression("", key);
        Assert.assertEquals("(x_lane_canary not in ('red')) or (x_lane_canary is null)", expression);
    }

    @Test
    public void testResetsSql92SubscriptionData() {
        createConfig();
        // test gray group
        SubscriptionData subscriptionData = new SubscriptionData();
        String key = RocketMqSubscriptionDataUtils.buildSubscribeScope("TEST_TOPIC", "group",
                "127.0.0.1:9876");
        RocketMqSubscriptionDataUtils.resetsSql92SubscriptionData(subscriptionData, key);
        Assert.assertEquals("(x_lane_canary in ('gray'))", subscriptionData.getSubString());

        // test auto model
        subscriptionData.setExpressionType(ExpressionType.SQL92);
        Map<String, Object> map = new HashMap<>();
        map.put("x_lane_tag", "red");
        map.put("x_lane_canary", "red");
        GrayConfigContextUtils.createMqGrayConfig(map, DynamicConfigEventType.CREATE);
        RocketMqSubscriptionDataUtils.resetsSql92SubscriptionData(subscriptionData, key);
        Assert.assertEquals("(x_lane_canary not in ('red')) or (x_lane_canary is null)",
                subscriptionData.getSubString());
    }

    @Test
    public void testResetAutoCheckGrayTagItems() {
        Map<String, Object> map = new HashMap<>();
        map.put("x_lane_tag", "red");
        map.put("x_lane_canary", "red");
        GrayConfigContextUtils.createMqGrayConfig(map, DynamicConfigEventType.CREATE);
        GrayTagItem item = new GrayTagItem();
        item.setConsumerGroupTag("red");
        List<GrayTagItem> grayTagItems = new ArrayList<>();
        grayTagItems.add(item);
        RocketMqConsumerClientConfig clientConfig = new RocketMqConsumerClientConfig("127.0.0.1:9876", "TEST_TOPIC",
                "consumerGroup");
        RocketMqSubscriptionDataUtils.resetAutoCheckGrayTagItems(grayTagItems, clientConfig);

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumerGroup");
        DefaultMQPushConsumerImpl pushConsumer = new DefaultMQPushConsumerImpl(consumer, null);
        consumer.setNamesrvAddr("127.0.0.1:9876");
        boolean grayTagChanged = RocketMqSubscriptionDataUtils.getGrayTagChangeFlag("TEST_TOPIC",
                getPushConsumerRebalanced(pushConsumer));

        Assert.assertTrue(grayTagChanged);
    }
}
