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

import com.netflix.zuul.context.RequestContext;

import io.sermant.router.common.utils.ThreadLocalUtils;
import io.sermant.router.spring.BaseTransmitConfigTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * Test ZuulServletInterceptor
 *
 * @author provenceee
 * @since 2023-02-28
 */
public class ZuulServletInterceptorTest extends BaseTransmitConfigTest {
    private final ZuulServletInterceptor interceptor;

    public ZuulServletInterceptorTest() {
        interceptor = new ZuulServletInterceptor();
    }

    /**
     * Reset the test data
     */
    @Before
    public void clear() {
        ThreadLocalUtils.removeRequestTag();
        ThreadLocalUtils.removeRequestData();
    }

    /**
     * Test the before method
     */
    @Test
    public void testBefore() {
        // RequestTag is null
        interceptor.before(null);
        Assert.assertEquals(0, RequestContext.getCurrentContext().getZuulRequestHeaders().size());

        // rRequestTag is not null
        ThreadLocalUtils.addRequestTag(Collections.singletonMap("bar", Collections.singletonList("foo")));
        interceptor.before(null);
        Assert.assertEquals(1, RequestContext.getCurrentContext().getZuulRequestHeaders().size());
        Assert.assertEquals("foo", RequestContext.getCurrentContext().getZuulRequestHeaders().get("bar"));
    }
}
