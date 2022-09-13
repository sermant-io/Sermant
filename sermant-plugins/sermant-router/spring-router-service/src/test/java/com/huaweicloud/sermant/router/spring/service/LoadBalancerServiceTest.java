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

package com.huaweicloud.sermant.router.spring.service;

import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.label.LabelCache;
import com.huaweicloud.sermant.router.config.label.entity.Match;
import com.huaweicloud.sermant.router.config.label.entity.MatchRule;
import com.huaweicloud.sermant.router.config.label.entity.MatchStrategy;
import com.huaweicloud.sermant.router.config.label.entity.Route;
import com.huaweicloud.sermant.router.config.label.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.label.entity.Rule;
import com.huaweicloud.sermant.router.config.label.entity.ValueMatch;
import com.huaweicloud.sermant.router.spring.TestDefaultServiceInstance;
import com.huaweicloud.sermant.router.spring.cache.AppCache;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试LoadBalancerServiceImpl
 *
 * @author provenceee
 * @since 2022-09-13
 */
public class LoadBalancerServiceTest {
    private final LoadBalancerService loadBalancerService;

    public LoadBalancerServiceTest() {
        loadBalancerService = new LoadBalancerServiceImpl();
        init();
    }

    @Test
    public void testGetTargetInstances() {
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        List<Object> targetInstances = loadBalancerService.getTargetInstances("foo", instances, null, header);
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));
    }

    @Test
    public void testGetMismatchInstances() {
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);
        instances.add(instance2);

        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar2"));
        List<Object> targetInstances = loadBalancerService.getTargetInstances("foo", instances, null, header);
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance1, targetInstances.get(0));
    }

    private void init() {
        ValueMatch valueMatch = new ValueMatch();
        valueMatch.setMatchStrategy(MatchStrategy.EXACT);
        valueMatch.setValues(Collections.singletonList("bar1"));
        MatchRule matchRule = new MatchRule();
        matchRule.setValueMatch(valueMatch);
        List<MatchRule> matchRuleList = new ArrayList<>();
        matchRuleList.add(matchRule);
        Map<String, List<MatchRule>> headers = new HashMap<>();
        headers.put("bar", matchRuleList);
        Match match = new Match();
        match.setHeaders(headers);
        Rule rule = new Rule();
        rule.setPrecedence(2);
        rule.setMatch(match);
        Route route = new Route();
        route.setWeight(100);
        Map<String, String> tags = new HashMap<>();
        tags.put("version", "1.0.1");
        route.setTags(tags);
        List<Route> routeList = new ArrayList<>();
        routeList.add(route);
        rule.setRoute(routeList);
        List<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule);
        Map<String, List<Rule>> map = new HashMap<>();
        map.put("foo", ruleList);
        RouterConfiguration configuration = LabelCache.getLabel(RouterConstant.SPRING_CACHE_NAME);
        configuration.resetRouteRule(map);
        AppCache.INSTANCE.setAppName("foo");
    }
}