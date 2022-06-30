/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.grace.interceptors;

import com.huawei.registry.config.grace.GraceConstants;
import com.huawei.registry.config.grace.GraceContext;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.netflix.zuul.context.RequestContext;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 测试网关zuul
 *
 * @author zhouss
 * @since 2022-07-01
 */
public class SpringZuulResponseInterceptorTest extends ResponseTest {
    /**
     * 测试url
     *
     * @throws NoSuchMethodException 不会抛出
     */
    @Test
    public void testZuul() throws NoSuchMethodException {
        init();
        final RequestContext requestContext = new RequestContext();
        RequestContext.testSetCurrentContext(requestContext);
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.when(response.getHeader(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT)).thenReturn(SHUTDOWN_ENDPOINT);
        Mockito.when(response.getHeader(GraceConstants.MARK_SHUTDOWN_SERVICE_NAME)).thenReturn("test");
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final Object[] arguments = {request, response};
        final ExecuteContext executeContext = ExecuteContext
                .forMemberMethod(this, this.getClass().getDeclaredMethod("testZuul"), arguments, null, null);
        final SpringZuulResponseInterceptor interceptor = new SpringZuulResponseInterceptor();
        interceptor.before(executeContext);
        interceptor.after(executeContext);
        Assert.assertFalse(RequestContext.getCurrentContext().isEmpty());
        Assert.assertTrue(GraceContext.INSTANCE.getGraceShutDownManager().isMarkedOffline(SHUTDOWN_ENDPOINT));
    }
}
