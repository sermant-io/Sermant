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

package com.huaweicloud.sermant.router.spring.interceptor;

import com.huaweicloud.sermant.router.common.utils.ThreadLocalUtils;

import com.netflix.zuul.context.RequestContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * 测试ZuulServletInterceptor
 *
 * @author provenceee
 * @since 2023-02-28
 */
public class ZuulServletInterceptorTest {
    private final ZuulServletInterceptor interceptor;

    public ZuulServletInterceptorTest() {
        interceptor = new ZuulServletInterceptor();
    }

    /**
     * 重置测试数据
     */
    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
    }

    /**
     * 测试before方法
     */
    @Test
    public void testBefore() {
        // requestTag为null
        interceptor.before(null);
        Assert.assertEquals(0, RequestContext.getCurrentContext().getZuulRequestHeaders().size());

        // requestTag不为null
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("bar", Collections.singletonList("foo")));
        interceptor.before(null);
        Assert.assertEquals(1, RequestContext.getCurrentContext().getZuulRequestHeaders().size());
        Assert.assertEquals("foo", RequestContext.getCurrentContext().getZuulRequestHeaders().get("bar"));
    }
}