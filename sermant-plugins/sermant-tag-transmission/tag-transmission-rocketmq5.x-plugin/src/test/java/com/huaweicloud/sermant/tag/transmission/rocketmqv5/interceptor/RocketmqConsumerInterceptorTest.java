/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.sermant.tag.transmission.rocketmqv5.interceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;

import org.apache.rocketmq.common.message.Message;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试RocketmqConsumerInterceptor
 *
 * @author tangle
 * @since 2023-07-27
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RocketmqConsumerInterceptor.class)
public class RocketmqConsumerInterceptorTest extends BaseInterceptorTest {
    private final RocketmqConsumerInterceptor interceptor;

    public RocketmqConsumerInterceptorTest() throws Exception {
        RocketmqConsumerInterceptor interceptorBase = new RocketmqConsumerInterceptor();
        interceptor = PowerMockito.spy(interceptorBase);
        PowerMockito.doReturn(true).when(interceptor, "isRocketMqStackTrace", Mockito.any());
    }

    @Test
    public void testIsAvailable() throws Exception {
        StackTraceElement[] stackTraceElements = new StackTraceElement[2];
        RocketmqConsumerInterceptor interceptorBase = new RocketmqConsumerInterceptor();

        // 测试可通过
        stackTraceElements[0] = new StackTraceElement("org.apache.rocketmq.common.message.Message", "xxxMethod",
                "xxxFile", 1);
        stackTraceElements[1] = new StackTraceElement("org.apache.xxx", "xxxMethod",
                "xxxFile", 1);
        boolean resultAccess = Whitebox.invokeMethod(interceptorBase, "isRocketMqStackTrace",
                (Object) stackTraceElements);
        Assert.assertTrue(resultAccess);

        // 测试不可通过
        stackTraceElements[0] = new StackTraceElement("org.apache.rocketmq.common.message.Message", "xxxMethod",
                "xxxFile", 1);
        stackTraceElements[1] = new StackTraceElement("org.apache.rocketmq", "xxxMethod",
                "xxxFile", 1);
        boolean resultRefuse = Whitebox.invokeMethod(interceptorBase, "isRocketMqStackTrace",
                (Object) stackTraceElements);
        Assert.assertFalse(resultRefuse);
    }

    @Test
    public void testRocketmqConsumer() {
        ExecuteContext context;
        Map<String, List<String>> addHeaders = new HashMap<>();
        Map<String, List<String>> tags = new HashMap<>();
        TrafficUtils.removeTrafficTag();

        // 无Headers无Tags
        context = buildContext(addHeaders, tags);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("id"));
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("name"));

        // 有Headers无Tags
        addHeaders.put("defaultKey", Collections.singletonList("defaultValue"));
        context = buildContext(addHeaders, tags);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("id"));
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("name"));

        // 有Headers有Tags
        tags.put("id", Collections.singletonList("testId001"));
        context = buildContext(addHeaders, tags);
        interceptor.before(context);
        Assert.assertEquals("testId001", TrafficUtils.getTrafficTag().getTag().get("id").get(0));
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("name"));

        // 第二次消费，测试tags是否污染其他消费
        tags.clear();
        List<String> names = new ArrayList<>();
        names.add("testName001");
        tags.put("name", names);
        context = buildContext(addHeaders, tags);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag().getTag().get("id"));
        Assert.assertEquals("testName001", TrafficUtils.getTrafficTag().getTag().get("name").get(0));

        // 测试TagTransmissionConfig开关关闭时
        TrafficUtils.removeTrafficTag();
        tagTransmissionConfig.setEnabled(false);
        interceptor.before(context);
        Assert.assertNull(TrafficUtils.getTrafficTag());
    }

    private ExecuteContext buildContext(Map<String, List<String>> addHeaders, Map<String, List<String>> tags) {
        Message message = new Message();
        for (Map.Entry<String, List<String>> entry : addHeaders.entrySet()) {
            for (String val : entry.getValue()) {
                message.putUserProperty(entry.getKey(), val);
            }
        }
        for (Map.Entry<String, List<String>> tag : tags.entrySet()) {
            for (String val : tag.getValue()) {
                message.putUserProperty(tag.getKey(), val);
            }
        }
        return ExecuteContext.forMemberMethod(
                message,
                null,
                null,
                null,
                null);
    }
}
