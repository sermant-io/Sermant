/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.router.spring.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * SpringRouterUtils test
 *
 * @author provenceee
 * @since 2024-11-20
 */
public class SpringRouterUtilsTest {
    /**
     * test getParametersByQuery
     */
    @Test
    public void testGetParametersByQuery() {
        String query = "a=b";
        Map<String, List<String>> parametersByQuery = SpringRouterUtils.getParametersByQuery(query);
        List<String> list = parametersByQuery.get("a");
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("b", list.get(0));

        query = "a=b&a=c";
        parametersByQuery = SpringRouterUtils.getParametersByQuery(query);
        list = parametersByQuery.get("a");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("b", list.get(0));
        Assert.assertEquals("c", list.get(1));

        query = "a=b&b=&a=c";
        parametersByQuery = SpringRouterUtils.getParametersByQuery(query);
        list = parametersByQuery.get("a");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("b", list.get(0));
        Assert.assertEquals("c", list.get(1));
        list = parametersByQuery.get("b");
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("", list.get(0));

        Assert.assertEquals(Collections.emptyMap(), SpringRouterUtils.getParametersByQuery(null));
    }
}
