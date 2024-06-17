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
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;
import io.sermant.mq.grayscale.utils.SubscriptionDataUtils;

import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * MqSubscriptionAutoCheckInterceptor test
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqSubscriptionAutoCheckInterceptorTest extends AbstactMqGrayTest {
    @Test
    public void testAfter() throws Exception {
        ConcurrentMap<String, SubscriptionData> map = new ConcurrentHashMap<>();
        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setExpressionType(SubscriptionDataUtils.EXPRESSION_TYPE_TAG);
        map.put("default", subscriptionData);
        ExecuteContext context = ExecuteContext.forMemberMethod(new Object(), null, null, null, null);
        context.afterMethod(map, new Throwable());
        MqGrayscaleConfigUtils.MQ_EXCLUDE_TAG_CHANGE_FLAG = true;
        ConfigContextUtils.createMqGrayConfig(null);
        MqSubscriptionAutoCheckInterceptor interceptor = new MqSubscriptionAutoCheckInterceptor();
        interceptor.after(context);
        Assert.assertEquals(SubscriptionDataUtils.EXPRESSION_TYPE_SQL92, subscriptionData.getExpressionType());
        MqGrayscaleConfigUtils.MQ_EXCLUDE_TAG_CHANGE_FLAG = true;
        subscriptionData.setExpressionType(SubscriptionDataUtils.EXPRESSION_TYPE_SQL92);
        interceptor.after(context);
        Assert.assertTrue(subscriptionData.getSubString().contains("test%gray"));

    }
}
