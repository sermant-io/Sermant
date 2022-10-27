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

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.config.entity.EnabledStrategy;
import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.MatchStrategy;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.entity.Strategy;
import com.huaweicloud.sermant.router.config.entity.ValueMatch;
import com.huaweicloud.sermant.router.spring.TestDefaultServiceInstance;
import com.huaweicloud.sermant.router.spring.cache.AppCache;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Arrays;
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
    private static LoadBalancerService loadBalancerService;

    private static RouterConfig config;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        config = new RouterConfig();
        config.setZone("foo");
        config.setRequestTags(Arrays.asList("foo", "bar", "version"));
        mockPluginConfigManager = Mockito.mockStatic(PluginConfigManager.class);
        mockPluginConfigManager.when(() -> PluginConfigManager.getPluginConfig(RouterConfig.class))
            .thenReturn(config);
        loadBalancerService = new LoadBalancerServiceImpl();
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockPluginConfigManager.close();
    }

    public LoadBalancerServiceTest() {
        initRule();
        EnabledStrategy strategy = ConfigCache.getEnabledStrategy(RouterConstant.SPRING_CACHE_NAME);
        strategy.reset(Strategy.ALL, Collections.emptyList());
    }

    /**
     * 重置
     */
    @Before
    public void reset() {
        config.setUseRequestRouter(false);
        config.setRequestTags(null);
    }

    /**
     * 测试getTargetInstancesByRules方法
     */
    @Test
    public void testGetTargetInstancesByRules() {
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

    /**
     * 测试getTargetInstancesByRequest方法
     */
    @Test
    public void testGetTargetInstancesByRequest() {
        config.setUseRequestRouter(true);
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0",
            Collections.singletonMap("bar", "bar1"));
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance
            .getTestDefaultServiceInstance("1.0.1", Collections.singletonMap("foo", "bar2"));
        instances.add(instance2);
        ServiceInstance instance3 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance3);
        Map<String, List<String>> header = new HashMap<>();

        // 测试无tags时
        List<Object> targetInstances = loadBalancerService.getTargetInstances("foo", instances, null, header);
        Assert.assertEquals(instances, targetInstances);

        // 设置tags
        config.setRequestTags(Arrays.asList("foo", "bar", "version"));

        // 匹配foo: bar2实例
        header.clear();
        header.put("foo", Collections.singletonList("bar2"));
        header.put("foo1", Collections.singletonList("bar2"));
        targetInstances = loadBalancerService.getTargetInstances("foo", instances, null, header);
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));

        // 匹配1.0.0版本实例
        header.clear();
        header.put("version", Collections.singletonList("1.0.0"));
        targetInstances = loadBalancerService.getTargetInstances("foo", instances, null, header);
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance1, targetInstances.get(0));
    }

    /**
     * 测试getTargetInstancesByRequest方法不匹配时
     */
    @Test
    public void testGetTargetInstancesByRequestWithMismatch() {
        config.setUseRequestRouter(true);
        config.setRequestTags(Arrays.asList("foo", "bar", "version"));
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0",
            Collections.singletonMap("foo", "bar1"));
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance
            .getTestDefaultServiceInstance("1.0.1", Collections.singletonMap("bar", "bar2"));
        instances.add(instance2);
        ServiceInstance instance3 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.2");
        instances.add(instance3);

        // 不匹配bar: bar1实例时，匹配没有bar标签的实例
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        List<Object> targetInstances = loadBalancerService.getTargetInstances("foo", instances, null, header);
        Assert.assertEquals(2, targetInstances.size());
        Assert.assertFalse(targetInstances.contains(instance2));

        // 不匹配bar: bar1实例时，优先匹配没有bar标签的实例，如果没有无bar标签的实例，则返回空列表
        List<Object> sameInstances = new ArrayList<>();
        ServiceInstance sameInstance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0",
            Collections.singletonMap("bar", "bar3"));
        sameInstances.add(sameInstance1);
        ServiceInstance sameInstance2 = TestDefaultServiceInstance
            .getTestDefaultServiceInstance("1.0.1", Collections.singletonMap("bar", "bar2"));
        sameInstances.add(sameInstance2);
        header.clear();
        header.put("bar", Collections.singletonList("bar1"));
        targetInstances = loadBalancerService.getTargetInstances("foo", sameInstances, null, header);
        Assert.assertEquals(0, targetInstances.size());

        // 不匹配version: 1.0.3实例时，返回所有版本的实例
        header.clear();
        header.put("version", Collections.singletonList("1.0.3"));
        targetInstances = loadBalancerService.getTargetInstances("foo", instances, null, header);
        Assert.assertEquals(3, targetInstances.size());

        // 不传入header时，匹配无标签实例
        header.clear();
        targetInstances = loadBalancerService.getTargetInstances("foo", instances, null, header);
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance3, targetInstances.get(0));

        // 不传入header时，优先匹配无标签实例，没有无标签实例时，返回全部实例
        header.clear();
        targetInstances = loadBalancerService.getTargetInstances("foo", sameInstances, null, header);
        Assert.assertEquals(sameInstances, targetInstances);
    }

    /**
     * 测试getTargetInstances方法只有一个下游时
     */
    @Test
    public void testGetTargetInstancesWithOneInstance() {
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        List<Object> targetInstances = loadBalancerService.getTargetInstances("foo", instances, null, header);
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instances, targetInstances);
    }

    /**
     * 测试getMismatchInstances方法
     */
    @Test
    public void testGetMismatchInstances() {
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);

        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar2"));
        List<Object> targetInstances = loadBalancerService.getTargetInstances("foo", instances, null, header);
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance1, targetInstances.get(0));
    }

    /**
     * 测试getZoneInstances方法
     */
    @Test
    public void testGetZoneInstances() {
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "bar");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", "foo");
        instances.add(instance2);

        List<Object> targetInstances = loadBalancerService.getZoneInstances("foo", instances, true);
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));

        // 测试关闭开关
        targetInstances = loadBalancerService.getZoneInstances("foo", instances, false);
        Assert.assertEquals(2, targetInstances.size());
        Assert.assertEquals(instances, targetInstances);
    }

    private void initRule() {
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
        RouterConfiguration configuration = ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME);
        configuration.resetRouteRule(map);
        AppCache.INSTANCE.setAppName("foo");
    }
}