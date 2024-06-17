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

import org.apache.rocketmq.client.hook.FilterMessageHook;
import org.apache.rocketmq.client.impl.consumer.PullAPIWrapper;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PullAPIWrapperMessageHookInterceptor test
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class PullApiWrapperMessageHookInterceptorTest extends AbstactMqGrayTest {
    @Test
    public void testAfter() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("serverGrayEnabled", false);
        ConfigContextUtils.createMqGrayConfig(map);
        MQClientInstance instance = Mockito.mock(MQClientInstance.class);
        PullAPIWrapper pullAPIWrapper = new PullAPIWrapper(instance, "group", false);
        List<FilterMessageHook> list = new ArrayList<>();
        Object[] arguments = new Object[]{list};
        ExecuteContext context = ExecuteContext.forMemberMethod(pullAPIWrapper, null, arguments, null, null);
        PullApiWrapperMessageHookInterceptor interceptor = new PullApiWrapperMessageHookInterceptor();
        interceptor.after(context);
        Assert.assertEquals(1, list.size());
    }
}
