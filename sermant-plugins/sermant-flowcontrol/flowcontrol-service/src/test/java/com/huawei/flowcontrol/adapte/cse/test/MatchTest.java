/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.adapte.cse.test;

import com.huawei.flowcontrol.common.adapte.cse.match.BusinessMatcher;
import com.huawei.flowcontrol.common.adapte.cse.match.MatchGroupResolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 方法匹配测试
 *
 * @author zhouss
 * @since 2021-12-24
 */
public class MatchTest extends BaseTest {

    BusinessMatcher matcher;

    @Before
    public void config() {
        final MatchGroupResolver matchGroupResolver = new MatchGroupResolver();
        matcher = matchGroupResolver.parseRule(MatchGroupResolver.CONFIG_KEY, buildRule("/login"), false, false);
    }

    @Test
    public void baseMatch() {
        final boolean match = matcher.match("/login", Collections.<String, String>emptyMap(), "GET");
        Assert.assertFalse(match);
    }

    @Test
    public void matchHeader() {
        final boolean matchHeader = matcher.match("/login", buildHeader(), "GET");
        Assert.assertTrue(matchHeader);
    }

    @Test
    public void testCompare() {
        final Map<String, String> headers = buildHeader();
        headers.put("key3", "1");
        final boolean result = matcher.match("/login", headers, "POST");
        Assert.assertFalse(result);
    }

    @Test
    public void testApiPath() {
        final String rule = buildRuleWithNoHeader("/test");
        final BusinessMatcher matcher = new MatchGroupResolver().parseRule(MatchGroupResolver.CONFIG_KEY, rule, false, false);
        Assert.assertTrue(matcher.match("/test", Collections.<String, String>emptyMap(), "GET"));
        Assert.assertFalse(matcher.match("/Notest", Collections.<String, String>emptyMap(), "GET"));
    }

    private Map<String, String> buildHeader() {
        final HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("key1", "bbb");
        headers.put("key2", "bcb");
        headers.put("key3","9");
        return headers;
    }

    private String buildRuleWithNoHeader(String api) {
        return "matches:\n" +
                "  - apiPath:\n" +
                "      prefix: " + api + "\n" +
                "    headers: {}\n" +
                "    method:\n" +
                "      - GET\n" +
                "      - PUT\n" +
                "      - POST\n" +
                "      - PATCH\n" +
                "    name: rule1\n" +
                "    showAlert: false\n" +
                "    uniqIndex: npkls";
    }

    private String buildRule(String api) {
        return "matches:\n" +
                "  - apiPath:\n" +
                "      prefix: " + api + "\n" +
                "    headers:\n" +
                "      key1:\n" +
                "        prefix: b\n" +
                "      key2:\n" +
                "        contains: c\n" +
                "      key3:\n" +
                "        compare: '>8'\n" +
                "    method:\n" +
                "      - GET\n" +
                "      - PUT\n" +
                "      - POST\n" +
                "      - PATCH\n" +
                "    name: rule1\n" +
                "    showAlert: false\n" +
                "    uniqIndex: npkls";
    }
}
