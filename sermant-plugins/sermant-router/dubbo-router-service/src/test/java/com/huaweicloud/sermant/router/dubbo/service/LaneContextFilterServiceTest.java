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

package com.huaweicloud.sermant.router.dubbo.service;

import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.EntireRule;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.MatchStrategy;
import com.huaweicloud.sermant.router.config.entity.Protocol;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.entity.ValueMatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试LaneContextFilterServiceImpl
 *
 * @author provenceee
 * @since 2023-02-27
 */
public class LaneContextFilterServiceTest {
    private final LaneContextFilterService service;

    public LaneContextFilterServiceTest() {
        service = new LaneContextFilterServiceImpl();
    }

    @Before
    public void clear() {
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
        DubboCache.INSTANCE.setAppName("foo");
    }

    /**
     * 测试规则无效时
     */
    @Test
    public void testWithInvalidConfiguration() {
        Map<String, List<String>> lane = service.getLane("", "", Collections.emptyMap(), null);
        Assert.assertEquals(Collections.emptyMap(), lane);
    }

    /**
     * 测试没有命中的规则时
     */
    @Test
    public void testWithEmptyRules() {
        initRules();
        Map<String, List<String>> lane = service.getLane("", "", Collections.emptyMap(), null);
        Assert.assertEquals(Collections.emptyMap(), lane);
    }

    /**
     * 测试规则不匹配时
     */
    @Test
    public void testWithEmptyRoutes() {
        initRules();
        Map<String, List<String>> lane = service.getLane("com.foo.test", "test", Collections.emptyMap(), null);
        Assert.assertEquals(Collections.emptyMap(), lane);
    }

    /**
     * 测试getLane方法
     */
    @Test
    public void testGetLane() {
        initRules();
        Map<String, List<String>> lane = service
                .getLane("com.foo.test", "test", Collections.singletonMap("bar", "bar1"), new Object[]{"foo1"});
        Assert.assertEquals(1, lane.size());
        Assert.assertEquals("flag1", lane.get("sermant-flag").get(0));
    }

    private void initRules() {
        ValueMatch valueMatch = new ValueMatch();
        valueMatch.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch.setValues(Collections.singletonList("bar1"));
        MatchRule matchRule = new MatchRule();
        matchRule.setValueMatch(valueMatch);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule);
        Map<String, List<MatchRule>> attachments = new HashMap<>();
        attachments.put("bar", matchRuleList);
        Match match = new Match();
        match.setMethod("test");
        match.setPath("com.foo.test");
        match.setProtocol(Protocol.DUBBO);
        match.setAttachments(attachments);
        ValueMatch argValueMatch = new ValueMatch();
        argValueMatch.setMatchStrategy(MatchStrategy.IN);
        argValueMatch.setValues(Collections.singletonList("foo1"));
        MatchRule argMatchRule = new MatchRule();
        argMatchRule.setValueMatch(argValueMatch);
        argMatchRule.setType("");
        List<MatchRule> argMatchRuleList = new ArrayList<>();
        argMatchRuleList.add(argMatchRule);
        Map<String, List<MatchRule>> args = new HashMap<>();
        args.put("args0", argMatchRuleList);
        match.setArgs(args);
        Rule rule = new Rule();
        rule.setPrecedence(2);
        rule.setMatch(match);
        Route route = new Route();
        route.setWeight(100);
        Map<String, String> tags = new HashMap<>();
        tags.put("sermant-flag", "flag1");
        route.setInjectTags(tags);
        List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        rule.setRoute(routeList);
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule);
        EntireRule entireRule = new EntireRule();
        entireRule.setRules(ruleList);
        entireRule.setKind(RouterConstant.LANE_MATCH_KIND);
        Map<String, List<EntireRule>> map = new HashMap<>();
        map.put("foo", Collections.singletonList(entireRule));
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME);
        configuration.resetRouteRule(map);
    }
}