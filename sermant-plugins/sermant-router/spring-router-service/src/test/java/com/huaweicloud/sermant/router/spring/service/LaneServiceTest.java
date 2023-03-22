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

package com.huaweicloud.sermant.router.spring.service;

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
import com.huaweicloud.sermant.router.spring.cache.AppCache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试LaneServiceImpl
 *
 * @author provenceee
 * @since 2023-02-27
 */
public class LaneServiceTest {
    private final LaneService service;

    public LaneServiceTest() {
        service = new LaneServiceImpl();
    }

    @Before
    public void clear() {
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
        AppCache.INSTANCE.setAppName("foo");
    }

    /**
     * 测试规则无效时
     */
    @Test
    public void testWithInvalidConfiguration() {
        Map<String, List<String>> lane = service.getLaneByParameterArray("", "", Collections.emptyMap(), null);
        Assert.assertEquals(Collections.emptyMap(), lane);

        lane = service.getLaneByParameterList("", "", Collections.emptyMap(), null);
        Assert.assertEquals(Collections.emptyMap(), lane);
    }

    /**
     * 测试没有命中的规则时
     */
    @Test
    public void testWithEmptyRules() {
        initRules();
        Map<String, List<String>> lane = service.getLaneByParameterArray("", "", Collections.emptyMap(), null);
        Assert.assertEquals(Collections.emptyMap(), lane);

        lane = service.getLaneByParameterList("", "", Collections.emptyMap(), null);
        Assert.assertEquals(Collections.emptyMap(), lane);
    }

    /**
     * 测试规则不匹配时
     */
    @Test
    public void testWithEmptyRoutes() {
        initRules();
        Map<String, List<String>> lane = service
                .getLaneByParameterArray("/foo/test", "get", Collections.emptyMap(), null);
        Assert.assertEquals(Collections.emptyMap(), lane);

        lane = service
                .getLaneByParameterList("/foo/test", "get", Collections.emptyMap(), null);
        Assert.assertEquals(Collections.emptyMap(), lane);
    }

    /**
     * 测试getLane方法
     */
    @Test
    public void testGetLane() {
        initRules();
        Map<String, List<String>> lane = service
                .getLaneByParameterArray("/foo/test", "get",
                        Collections.singletonMap("bar", Collections.singletonList("bar1")),
                        Collections.singletonMap("foo", new String[]{"foo1"}));
        Assert.assertEquals(1, lane.size());
        Assert.assertEquals("flag1", lane.get("sermant-flag").get(0));

        lane = service
                .getLaneByParameterList("/foo/test", "get",
                        Collections.singletonMap("bar", Collections.singletonList("bar1")),
                        Collections.singletonMap("foo", Collections.singletonList("foo1")));
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
        match.setMethod("Get");
        match.setPath("^/foo.*");
        match.setProtocol(Protocol.HTTP);
        match.setHeaders(attachments);
        ValueMatch parameterValueMatch = new ValueMatch();
        parameterValueMatch.setMatchStrategy(MatchStrategy.IN);
        parameterValueMatch.setValues(Collections.singletonList("foo1"));
        MatchRule parameterMatchRule = new MatchRule();
        parameterMatchRule.setValueMatch(parameterValueMatch);
        parameterMatchRule.setType("");
        List<MatchRule> parameterMatchRuleList = new ArrayList<>();
        parameterMatchRuleList.add(parameterMatchRule);
        Map<String, List<MatchRule>> args = new HashMap<>();
        args.put("foo", parameterMatchRuleList);
        match.setParameters(args);
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
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME);
        configuration.resetRouteRule(map);
    }
}