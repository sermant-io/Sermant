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

    /**
     * 前置配置
     */
    @Before
    public void config() {
        final MatchGroupResolver matchGroupResolver = new MatchGroupResolver();
        matcher = matchGroupResolver.parseRule(MatchGroupResolver.CONFIG_KEY, buildRule("/login"), false, false);
    }

    /**
     * 基本匹配测试
     */
    @Test
    public void baseMatch() {
        final boolean match = matcher.match("/login", Collections.<String, String>emptyMap(), "GET");
        Assert.assertFalse(match);
    }

    /**
     * 匹配请求头测试
     */
    @Test
    public void matchHeader() {
        final boolean matchHeader = matcher.match("/login", buildHeader(), "GET");
        Assert.assertTrue(matchHeader);
    }

    /**
     * 测试比较能力
     */
    @Test
    public void testCompare() {
        final Map<String, String> headers = buildHeader();
        headers.put("key3", "1");
        final boolean result = matcher.match("/login", headers, "POST");
        Assert.assertFalse(result);
    }

    /**
     * 测试api路径匹配
     */
    @Test
    public void testApiPath() {
        final String rule = buildRuleWithNoHeader("/test");
        final BusinessMatcher businessMatcher = new MatchGroupResolver()
            .parseRule(MatchGroupResolver.CONFIG_KEY, rule, false, false);
        Assert.assertTrue(businessMatcher.match("/test", Collections.<String, String>emptyMap(), "GET"));
        Assert.assertFalse(businessMatcher.match("/Notest", Collections.<String, String>emptyMap(), "GET"));
    }

    private Map<String, String> buildHeader() {
        final HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("key1", "bbb");
        headers.put("key2", "bcb");
        headers.put("key3", "9");
        return headers;
    }

    @SuppressWarnings("checkstyle:OperatorWrap")
    private String buildRuleWithNoHeader(String api) {
        return "matches:" + System.lineSeparator() +
            "  - apiPath:" + System.lineSeparator() +
            "      prefix: " + api + System.lineSeparator() +
            "    headers: {}" + System.lineSeparator() +
            "    method:" + System.lineSeparator() +
            "      - GET" + System.lineSeparator() +
            "      - PUT" + System.lineSeparator() +
            "      - POST" + System.lineSeparator() +
            "      - PATCH" + System.lineSeparator() +
            "    name: rule1" + System.lineSeparator() +
            "    showAlert: false" + System.lineSeparator() +
            "    uniqIndex: npkls";
    }

    @SuppressWarnings("checkstyle:OperatorWrap")
    private String buildRule(String api) {
        return "matches:" + System.lineSeparator() +
            "  - apiPath:" + System.lineSeparator() +
            "      prefix: " + api + System.lineSeparator() +
            "    headers:" + System.lineSeparator() +
            "      key1:" + System.lineSeparator() +
            "        prefix: b" + System.lineSeparator() +
            "      key2:" + System.lineSeparator() +
            "        contains: c" + System.lineSeparator() +
            "      key3:" + System.lineSeparator() +
            "        compare: '>8'" + System.lineSeparator() +
            "    method:" + System.lineSeparator() +
            "      - GET" + System.lineSeparator() +
            "      - PUT" + System.lineSeparator() +
            "      - POST" + System.lineSeparator() +
            "      - PATCH" + System.lineSeparator() +
            "    name: rule1" + System.lineSeparator() +
            "    showAlert: false" + System.lineSeparator() +
            "    uniqIndex: npkls";
    }
}
