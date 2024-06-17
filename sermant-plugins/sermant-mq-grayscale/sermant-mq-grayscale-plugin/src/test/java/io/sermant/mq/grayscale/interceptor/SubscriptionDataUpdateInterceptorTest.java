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
import io.sermant.mq.grayscale.utils.SubscriptionDataUtils;

import org.apache.rocketmq.common.filter.FilterAPI;
import org.apache.rocketmq.common.protocol.heartbeat.SubscriptionData;
import org.junit.Assert;
import org.junit.Test;

/**
 * SubscriptionDataUpdateInterceptor test
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class SubscriptionDataUpdateInterceptorTest extends AbstactMqGrayTest {
    @Test
    public void testAfter() throws Exception {
        ConfigContextUtils.createMqGrayConfig(null);
        FilterAPI filterAPI = new FilterAPI();
        SubscriptionDataUpdateInterceptor interceptor = new SubscriptionDataUpdateInterceptor();
        ExecuteContext context = ExecuteContext.forMemberMethod(filterAPI, null, null, null, null);
        SubscriptionData data = new SubscriptionData();
        data.setExpressionType(SubscriptionDataUtils.EXPRESSION_TYPE_TAG);
        context.afterMethod(data, new Throwable());
        interceptor.after(context);
        Assert.assertEquals(SubscriptionDataUtils.EXPRESSION_TYPE_SQL92, data.getExpressionType());
        data.setExpressionType(SubscriptionDataUtils.EXPRESSION_TYPE_SQL92);
        interceptor.after(context);
        Assert.assertTrue(data.getSubString().contains("test%gray"));
    }
}
