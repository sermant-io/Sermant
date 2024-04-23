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

package io.sermant.router.spring.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.router.common.request.RequestData;
import io.sermant.router.common.request.RequestTag;
import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.BaseTransmitConfigTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ReactiveTypeHandlerInterceptor
 *
 * @author provenceee
 * @since 2023-06-13
 */
public class ReactiveTypeHandlerInterceptorTest extends BaseTransmitConfigTest {
    private final ReactiveTypeHandlerInterceptor interceptor;

    private final ExecuteContext context;

    public ReactiveTypeHandlerInterceptorTest() {
        interceptor = new ReactiveTypeHandlerInterceptor();
        context = ExecuteContext.forMemberMethod(new Object(), null, null, null, null);
    }

    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
    }

    @Test
    public void testAfter() {
        ThreadLocalUtils.setRequestTag(new RequestTag(null));
        ThreadLocalUtils.setRequestData(new RequestData(null, null, null));

        interceptor.after(context);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
        Assert.assertNull(ThreadLocalUtils.getRequestData());
    }

    @Test
    public void testOnThrow() {
        ThreadLocalUtils.setRequestTag(new RequestTag(null));
        ThreadLocalUtils.setRequestData(new RequestData(null, null, null));

        interceptor.onThrow(context);
        Assert.assertNull(ThreadLocalUtils.getRequestTag());
        Assert.assertNull(ThreadLocalUtils.getRequestData());
    }
}