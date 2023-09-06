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

package com.huaweicloud.sermant.tag.transmission.interceptors.crossthread;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.BaseTest;
import com.huaweicloud.sermant.tag.transmission.RunnableAndCallable;
import com.huaweicloud.sermant.tag.transmission.interceptors.crossthread.ScheduledExecutorServiceInterceptor;
import com.huaweicloud.sermant.tag.transmission.pojo.TrafficMessage;
import com.huaweicloud.sermant.tag.transmission.wrapper.CallableWrapper;
import com.huaweicloud.sermant.tag.transmission.wrapper.RunnableAndCallableWrapper;
import com.huaweicloud.sermant.tag.transmission.wrapper.RunnableWrapper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * 测试ScheduledExecutorService
 *
 * @author provenceee
 * @since 2023-06-13
 */
public class ScheduledExecutorServiceInterceptorTest extends BaseTest {
    private final ScheduledExecutorServiceInterceptor interceptor;

    private final ExecuteContext context;

    private final Object[] arguments;

    public ScheduledExecutorServiceInterceptorTest() {
        arguments = new Object[1];
        interceptor = new ScheduledExecutorServiceInterceptor();
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }

    @Test
    public void testBefore() {
        // 测试null
        interceptor.before(context);
        Assert.assertNull(context.getArguments()[0]);

        // 测试没有路由数据
        Runnable runnable = () -> {
        };
        arguments[0] = runnable;
        interceptor.before(context);
        Assert.assertEquals(runnable, context.getArguments()[0]);

        // 存入路由数据
        TrafficUtils.updateTrafficTag(Collections.singletonMap("foo", Collections.singletonList("bar")));

        // 测试已经包装过了
        TrafficMessage trafficMessage = new TrafficMessage(null, null);
        RunnableWrapper<?> runnableWrapper = new RunnableWrapper<>(null, trafficMessage, false, null);
        arguments[0] = runnableWrapper;
        interceptor.before(context);
        Assert.assertEquals(runnableWrapper, context.getArguments()[0]);

        // 包装RunnableAndCallable
        RunnableAndCallable runnableAndCallable = new RunnableAndCallable();
        arguments[0] = runnableAndCallable;
        interceptor.before(context);
        Assert.assertTrue(context.getArguments()[0] instanceof RunnableAndCallableWrapper);

        // 包装Runnable
        Runnable runnable1 = () -> {
        };
        arguments[0] = runnable1;
        interceptor.before(context);
        Assert.assertTrue(context.getArguments()[0] instanceof RunnableWrapper);

        // 包装Callable
        Callable<Object> callable = () -> null;
        arguments[0] = callable;
        interceptor.before(context);
        Assert.assertTrue(context.getArguments()[0] instanceof CallableWrapper);
    }

    @After
    public void clear() {
        TrafficUtils.removeTrafficData();
        TrafficUtils.removeTrafficTag();
    }
}