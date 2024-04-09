/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import java.util.List;

/**
 * Test WebClientBuilderInterceptor
 *
 * @author provenceee
 * @since 2024-01-16
 */
public class WebClientBuilderInterceptorTest {
    private final WebClientBuilderInterceptor interceptor;

    private final ExecuteContext context;

    private final Builder builder;

    public WebClientBuilderInterceptorTest() {
        builder = WebClient.builder();
        interceptor = new WebClientBuilderInterceptor();
        context = ExecuteContext.forMemberMethod(builder, null, null, null, null);
    }

    @Test
    public void testBefore() {
        // When no filter exists
        interceptor.before(context);
        List<ExchangeFilterFunction> list = (List<ExchangeFilterFunction>) ReflectUtils
                .getFieldValue(builder, "filters").orElse(null);
        Assert.assertNotNull(list);
        Assert.assertTrue(list.get(0) instanceof RouterExchangeFilterFunction);

        // When the RouterExchangeFilterFunction already exists
        interceptor.before(context);
        list = (List<ExchangeFilterFunction>) ReflectUtils.getFieldValue(builder, "filters").orElse(null);
        Assert.assertNotNull(list);
        Assert.assertTrue(list.get(0) instanceof RouterExchangeFilterFunction);
        Assert.assertEquals(1, list.size());

        // Clear the data
        ReflectUtils.setFieldValue(builder, "filters", null);

        // Test the case where you already have a filter
        ExchangeFilterFunction function = (clientRequest, exchangeFunction) -> exchangeFunction.exchange(clientRequest);
        builder.filter(function);
        interceptor.before(context);
        list = (List<ExchangeFilterFunction>) ReflectUtils.getFieldValue(builder, "filters").orElse(null);
        Assert.assertNotNull(list);
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.get(0) instanceof RouterExchangeFilterFunction);
        Assert.assertEquals(function, list.get(1));
    }
}