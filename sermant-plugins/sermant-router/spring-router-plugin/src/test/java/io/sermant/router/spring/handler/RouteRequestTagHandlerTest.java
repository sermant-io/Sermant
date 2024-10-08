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

package io.sermant.router.spring.handler;

import io.sermant.router.spring.handler.AbstractRequestTagHandler.Keys;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test RouteInterceptorHandler
 *
 * @author provenceee
 * @since 2023-02-28
 */
public class RouteRequestTagHandlerTest {
    private final RouteRequestTagHandler handler;

    public RouteRequestTagHandlerTest() {
        handler = new RouteRequestTagHandler();
    }

    /**
     * Test the getRequestTag method
     */
    @Test
    public void testGetRequestTag() {
        // Normal
        Map<String, List<String>> headers = new HashMap<>();
        Set<String> matchKeys = new HashSet<>();
        matchKeys.add("bar");
        matchKeys.add("foo");
        headers.put("bar", Collections.singletonList("bar1"));
        headers.put("foo", Collections.singletonList("foo1"));
        Map<String, List<String>> requestTag = handler.getRequestTag("", "", headers, null, new Keys(matchKeys, null));
        Assert.assertNotNull(requestTag);
        Assert.assertEquals(2, requestTag.size());
        Assert.assertEquals("bar1", requestTag.get("bar").get(0));
        Assert.assertEquals("foo1", requestTag.get("foo").get(0));

        // Test matchKeys as empty
        requestTag = handler.getRequestTag("", "", null, null, new Keys(null, null));
        Assert.assertEquals(Collections.emptyMap(), requestTag);
    }
}
