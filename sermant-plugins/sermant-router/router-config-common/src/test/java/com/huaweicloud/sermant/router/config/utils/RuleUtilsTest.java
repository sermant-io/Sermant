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

package com.huaweicloud.sermant.router.config.utils;

import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;

import com.alibaba.fastjson.JSONArray;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 测试规则工具类
 *
 * @author provenceee
 * @since 2022-08-11
 */
public class RuleUtilsTest {
    private List<Rule> list;

    @Before
    public void before() {
        String json = "[{\"precedence\":3,\"match\":{\"headers\":{\"region\":{\"parameterName\":\"region\",\"exact\":1,"
            + "\"operationMark\":\"~\",\"caseInsensitive\":false},\"id\":{\"parameterName\":\"region\",\"regex\":\"*\","
            + "\"operationMark\":\"~\",\"caseInsensitive\":false},\"name\":{\"parameterName\":\"region\","
            + "\"exact\":\"test\",\"operationMark\":\"~\",\"caseInsensitive\":false}}},\"route\":[{\"name\":\"foo\","
            + "\"weight\":100,\"tags\":{\"version\":\"1.0.1\"}}]},{\"precedence\":2,\"route\":[{\"name\":\"bar\","
            + "\"weight\":100,\"tags\":{\"version\":\"1.0.0\"}}]}]";
        list = Collections.unmodifiableList(JSONArray.parseArray(json, Rule.class));
    }

    /**
     * 测试获取所有标签
     */
    @Test
    public void testGetTags() {
        List<Map<String, String>> tags = RuleUtils.getTags(list, false);
        Assert.assertEquals(2, tags.size());
        Assert.assertEquals("1.0.1", tags.get(0).get("version"));
        Assert.assertEquals("1.0.0", tags.get(1).get("version"));
    }

    /**
     * 测试初始化需要缓存的key
     */
    @Test
    public void testInitHeaderKeys() {
        RouterConfiguration configuration = new RouterConfiguration();
        RuleUtils.initMatchKeys(configuration);
        Assert.assertTrue(RuleUtils.getMatchKeys().isEmpty());
        Map<String, List<Rule>> map = new HashMap<>();
        map.put("test", list);
        configuration.resetRouteRule(map);
        RuleUtils.initMatchKeys(configuration);
        Set<String> keys = RuleUtils.getMatchKeys();
        Assert.assertEquals(3, keys.size());
    }

    /**
     * 测试更新header key
     */
    @Test
    public void testUpdateHeaderKeys() {
        Assert.assertTrue(RuleUtils.getMatchKeys().isEmpty());
        RuleUtils.updateMatchKeys("test", list);
        Set<String> keys = RuleUtils.getMatchKeys();
        Assert.assertEquals(3, keys.size());
        RuleUtils.updateMatchKeys("test", Collections.emptyList());
        Assert.assertTrue(RuleUtils.getMatchKeys().isEmpty());
    }
}