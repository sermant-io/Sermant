/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.interceptors.httpclient;

import com.huawei.discovery.utils.HttpAsyncUtils;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * 获取handler测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class HttpAsyncClient4xHandlerInterceptorTest {
    @Test
    public void after() throws Exception {
        final HttpAsyncClient4xHandlerInterceptor interceptor = new HttpAsyncClient4xHandlerInterceptor();
        final Object target = new Object();
        final ExecuteContext executeContext = buildContext(target);
        interceptor.before(executeContext);
        interceptor.onThrow(executeContext);
        interceptor.after(executeContext);
        Assert.assertNull(HttpAsyncUtils.getOrCreateContext().getHandler());
        interceptor.after(executeContext);
        Assert.assertEquals(HttpAsyncUtils.getOrCreateContext().getHandler(), target);
    }

    @After
    public void tearDown() throws Exception {
        HttpAsyncUtils.remove();
    }

    private ExecuteContext buildContext(Object target) throws NoSuchMethodException {
        return ExecuteContext.forMemberMethod(target, String.class.getDeclaredMethod("trim"), null,
                null, null);
    }
}
