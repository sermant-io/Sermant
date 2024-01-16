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

package com.huaweicloud.sermant.router.spring.strategy;

import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.spring.TestDefaultServiceInstance;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试RuleStrategyHandler
 *
 * @author provenceee
 * @since 2022-09-09
 */
public class RuleStrategyHandlerTest {
    private final List<Route> routes;

    private final Rule rule;

    /**
     * 构造方法
     */
    public RuleStrategyHandlerTest() {
        rule = new Rule();
        routes = new ArrayList<>();
        Map<String, String> tags1 = new HashMap<>();
        tags1.put("version", "0.0.1");
        Route route1 = new Route();
        route1.setTags(tags1);
        route1.setWeight(100);
        routes.add(route1);
        Map<String, String> tags2 = new HashMap<>();
        tags2.put("version", "0.0.2");
        Route route2 = new Route();
        route2.setTags(tags2);
        route2.setWeight(100);
        routes.add(route2);
        rule.setRoute(routes);
    }

    /**
     * 测试命中0.0.1版本实例的情况
     */
    @Test
    public void testMatchV1() {
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.1");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.2");
        instances.add(instance2);
        List<Object> matchInvoker = RuleStrategyHandler.INSTANCE.getFlowMatchInstances("foo", instances, rule);
        Assert.assertEquals(100, routes.get(0).getWeight().intValue());
        Assert.assertEquals(1, matchInvoker.size());
        Assert.assertEquals(instance1, matchInvoker.get(0));
    }

    /**
     * 测试命中0.0.1版本实例的情况
     */
    @Test
    public void testMatchV1ByRequest() {
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.1");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.2");
        instances.add(instance2);
        List<Object> matchInvoker = RuleStrategyHandler.INSTANCE.getMatchInstancesByRequest("foo", instances,
                Collections.singletonMap("version", "0.0.1"));
        Assert.assertEquals(1, matchInvoker.size());
        Assert.assertEquals(instance1, matchInvoker.get(0));
    }

    /**
     * 测试未命中0.0.1版本实例的情况
     */
    @Test
    public void testMismatchV1() {
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.1");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.2");
        instances.add(instance2);
        routes.get(0).setWeight(0);

        // 测试匹配上路由，没有随机到实例的情况
        List<Object> matchInstances = RuleStrategyHandler.INSTANCE.getFlowMatchInstances("foo", instances, rule);
        Assert.assertEquals(1, matchInstances.size());
        Assert.assertEquals(instance2, matchInstances.get(0));

        // 测试没有匹配上路由，选取不匹配标签的实例的情况
        List<Map<String, String>> tags = new ArrayList<>();
        tags.add(routes.get(0).getTags());
        List<Object> mismatchInstances = RuleStrategyHandler.INSTANCE
                .getMismatchInstances("foo", instances, tags, true);
        Assert.assertEquals(1, mismatchInstances.size());
        Assert.assertEquals(instance2, mismatchInstances.get(0));
    }

    /**
     * rule中route有命中tag，但是没有符合版本的实例，invoker命中fallback版本实例的情况
     */
    @Test
    public void testMatchV1Fallback() {
        setFallbackRoute();

        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.3");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.4");
        instances.add(instance2);

        // Route随机命中route1，但是没有0.0.1版本实例；命中fallback，返回fallback实例信息
        List<Object> matchInvoker = RuleStrategyHandler.INSTANCE.getFlowMatchInstances("foo", instances, rule);
        Assert.assertEquals(100, routes.get(0).getWeight().intValue());
        Assert.assertEquals(1, matchInvoker.size());
        Assert.assertEquals(instance1, matchInvoker.get(0));
    }

    private void setFallbackRoute() {
        List<Route> fallback = new ArrayList<>();
        Map<String, String> tags = new HashMap<>();
        tags.put(RouterConstant.VERSION, "0.0.3");
        Route route = new Route();
        route.setTags(tags);
        route.setWeight(100);
        fallback.add(route);
        rule.setFallback(fallback);
    }

    /**
     * rule中设置route、fallback，且权重均有命中tag，但是invoker均未命中版本实例的情况
     */
    @Test
    public void testSpringV1NotMathRouteFallback() {
        setFallbackRoute();

        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.2");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.4");
        instances.add(instance2);
        // Route随机命中route1，但是没有0.0.1版本实例，fallback也未命中tag实例，所以返回全部实例信息
        List<Object> matchInvoker = RuleStrategyHandler.INSTANCE.getFlowMatchInstances("foo", instances, rule);
        Assert.assertEquals(2, matchInvoker.size());
    }

    /**
     * rule中设置routes但权重计算未命中，invoker命中fallback版本实例的情况
     */
    @Test
    public void testSpringV1MathFallback() {
        setFallbackRoute();
        routes.get(0).setWeight(0);
        routes.get(1).setWeight(0);

        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.2");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.3");
        instances.add(instance2);
        // Route计算权重均未命中tag，fallback权重计算命中tag，返回fallback规则命中实例信息
        List<Object> matchInvoker = RuleStrategyHandler.INSTANCE.getFlowMatchInstances("foo", instances, rule);
        Assert.assertEquals(1, matchInvoker.size());
        Assert.assertEquals(instance2, matchInvoker.get(0));
    }

    /**
     * rule中设置routes但权重计算未命中，同时fallback也未命中实例，invoker返回未设置规则版本号版本实例的情况
     */
    @Test
    public void testSpringV1BothNotMathFallbackRoute() {
        setFallbackRoute();
        routes.get(0).setWeight(0);
        routes.get(1).setWeight(0);

        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.1");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("0.0.4");
        instances.add(instance2);
        // Route计算权重均未命中tag，fallback权重计算也未命中tag，返回route中未设置tag实例信息
        List<Object> matchInvoker = RuleStrategyHandler.INSTANCE.getFlowMatchInstances("foo", instances, rule);
        Assert.assertEquals(1, matchInvoker.size());
        Assert.assertEquals(instance2, matchInvoker.get(0));
    }
}