/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.crossthread.interceptors;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.tag.TrafficUtils;
import com.huaweicloud.sermant.tag.transmission.crossthread.pojo.TrafficMessage;
import com.huaweicloud.sermant.tag.transmission.crossthread.wrapper.CallableWrapper;
import com.huaweicloud.sermant.tag.transmission.crossthread.wrapper.RunnableAndCallableWrapper;
import com.huaweicloud.sermant.tag.transmission.crossthread.wrapper.RunnableWrapper;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.Callable;

/**
 * test ExecutorInterceptor
 *
 * @author provenceee
 * @since 2023-05-26
 */
public class ExecutorInterceptorTest extends BaseTest {
    private final ExecutorInterceptor interceptor;

    private final ExecuteContext context;

    private final Object[] arguments;

    public ExecutorInterceptorTest() {
        arguments = new Object[1];
        interceptor = new ExecutorInterceptor();
        context = ExecuteContext.forMemberMethod(new Object(), null, arguments, null, null);
    }

    @Test
    public void testBefore() {
        // test null
        interceptor.before(context);
        Assert.assertNull(context.getArguments()[0]);

        // the test has no routing data
        Runnable runnable = () -> {
        };
        arguments[0] = runnable;
        interceptor.before(context);
        Assert.assertEquals(runnable, context.getArguments()[0]);

        // stored routing data
        TrafficUtils.updateTrafficTag(Collections.singletonMap("foo", Collections.singletonList("bar")));

        // test: wrapped
        TrafficMessage trafficMessage = new TrafficMessage(null, null);
        RunnableWrapper<?> runnableWrapper = new RunnableWrapper<>(null, trafficMessage, false, null);
        arguments[0] = runnableWrapper;
        interceptor.before(context);
        Assert.assertEquals(runnableWrapper, context.getArguments()[0]);

        // wrap RunnableAndCallable
        RunnableAndCallable runnableAndCallable = new RunnableAndCallable();
        arguments[0] = runnableAndCallable;
        interceptor.before(context);
        Assert.assertTrue(context.getArguments()[0] instanceof RunnableAndCallableWrapper);

        // wrap Runnable
        Runnable runnable1 = () -> {
        };
        arguments[0] = runnable1;
        interceptor.before(context);
        Assert.assertTrue(context.getArguments()[0] instanceof RunnableWrapper);

        // wrap Callable
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