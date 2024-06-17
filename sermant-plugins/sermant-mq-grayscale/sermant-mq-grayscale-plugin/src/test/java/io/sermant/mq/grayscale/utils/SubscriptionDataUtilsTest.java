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

package io.sermant.mq.grayscale.utils;

import io.sermant.mq.grayscale.AbstactMqGrayTest;
import io.sermant.mq.grayscale.ConfigContextUtils;

import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * MqGrayscaleConfigUtils test
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class SubscriptionDataUtilsTest extends AbstactMqGrayTest {
    @Test
    public void testBuildSQL92ExpressionByTags() {
        Set<String> tagsSet = new HashSet<>();
        tagsSet.add("test%gray");
        String tags = SubscriptionDataUtils.buildSql92ExpressionByTags(tagsSet);
        Assert.assertEquals(" ( TAGS is not null and TAGS in ('test%gray')  ) ", tags);
    }

    @Test
    public void testAddMseGrayTagsToSQL92Expression() {
        ConfigContextUtils.createMqGrayConfig(null);
        String data = SubscriptionDataUtils.addMseGrayTagsToSql92Expression("");
        Assert.assertEquals(" ( ( micro_service_gray_tag in ('test%gray') ) or ( micro_traffic_gray_tag is not null )  "
                + ") ", data);

        Map<String, Object> map = new HashMap<>();
        map.put("grayscale.environmentMatch.exact", new ArrayList<>());
        ConfigContextUtils.createMqGrayConfig(map);
        data = SubscriptionDataUtils.addMseGrayTagsToSql92Expression("");
        Assert.assertEquals(" ( ( micro_service_gray_tag not in ('test%gray')  ) or ( micro_service_gray_tag is null "
                + ")  ) ", data);

        map = new HashMap<>();
        map.put("grayscale.environmentMatch.exact", new ArrayList<>());
        map.put("base.messageFilter.consumeType", "base");
        ConfigContextUtils.createMqGrayConfig(map);
        data = SubscriptionDataUtils.addMseGrayTagsToSql92Expression("");
        Assert.assertEquals(" ( ( micro_service_gray_tag is null  ) and ( micro_traffic_gray_tag is null )  ) ",
                data);

    }

    @Test
    public void testResetsSQL92SubscriptionData() {
        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setExpressionType("TAG");
        SubscriptionDataUtils.resetsSql92SubscriptionData(subscriptionData);
        Assert.assertEquals(subscriptionData.getExpressionType(), SubscriptionDataUtils.EXPRESSION_TYPE_SQL92);
    }
}
