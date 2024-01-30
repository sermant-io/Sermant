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

package com.huaweicloud.sermant.router.dubbo.strategy;

import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.entity.Route;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.dubbo.AlibabaInvoker;
import com.huaweicloud.sermant.router.dubbo.ApacheInvoker;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签路由策略测试
 *
 * @author provenceee
 * @since 2022-03-22
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
        tags1.put(RouterConstant.META_VERSION_KEY, "0.0.1");
        Route route1 = new Route();
        route1.setTags(tags1);
        route1.setWeight(100);
        routes.add(route1);
        Map<String, String> tags2 = new HashMap<>();
        tags2.put(RouterConstant.META_VERSION_KEY, "0.0.2");
        Route route2 = new Route();
        route2.setTags(tags2);
        route2.setWeight(100);
        routes.add(route2);
        rule.setRoute(routes);
    }

    /**
     * 测试alibaba invoker命中0.0.1版本实例的情况
     */
    @Test
    public void testAlibabaV1() {
        List<Object> invokers = new ArrayList<>();
        AlibabaInvoker<Object> invoker1 = new AlibabaInvoker<>("0.0.1");
        invokers.add(invoker1);
        AlibabaInvoker<Object> invoker2 = new AlibabaInvoker<>("0.0.2");
        invokers.add(invoker2);
        List<Object> matchInvokers = RuleStrategyHandler.INSTANCE.getFlowMatchInvokers("foo", invokers, rule);
        Assert.assertEquals(100, routes.get(0).getWeight().intValue());
        Assert.assertEquals(1, matchInvokers.size());
        Assert.assertEquals(invoker1, matchInvokers.get(0));
    }

    /**
     * 测试alibaba invoker命中0.0.1版本实例的情况
     */
    @Test
    public void testAlibabaV1ByRequest() {
        List<Object> invokers = new ArrayList<>();
        AlibabaInvoker<Object> invoker1 = new AlibabaInvoker<>("0.0.1");
        invokers.add(invoker1);
        AlibabaInvoker<Object> invoker2 = new AlibabaInvoker<>("0.0.2");
        invokers.add(invoker2);
        List<Object> matchInvokers = RuleStrategyHandler.INSTANCE.getMatchInvokersByRequest("foo", invokers,
                Collections.singletonMap(RouterConstant.META_VERSION_KEY, "0.0.1"));
        Assert.assertEquals(1, matchInvokers.size());
        Assert.assertEquals(invoker1, matchInvokers.get(0));
    }

    /**
     * 测试alibaba invoker未命中0.0.1版本实例的情况
     */
    @Test
    public void testAlibabaMismatch() {
        List<Object> invokers = new ArrayList<>();
        AlibabaInvoker<Object> invoker1 = new AlibabaInvoker<>("0.0.1");
        invokers.add(invoker1);
        AlibabaInvoker<Object> invoker2 = new AlibabaInvoker<>("0.0.2");
        invokers.add(invoker2);
        routes.get(0).setWeight(0);

        // 测试匹配上路由，没有随机到实例的情况
        List<Object> matchInvokers = RuleStrategyHandler.INSTANCE.getFlowMatchInvokers("foo", invokers, rule);
        Assert.assertEquals(1, matchInvokers.size());
        Assert.assertEquals(invoker2, matchInvokers.get(0));

        // 测试没有匹配上路由，选取不匹配标签的实例的情况
        List<Map<String, String>> tags = new ArrayList<>();
        tags.add(routes.get(0).getTags());
        List<Object> mismatchInvokers = RuleStrategyHandler.INSTANCE.getMismatchInvokers("foo", invokers, tags, true);
        Assert.assertEquals(1, mismatchInvokers.size());
        Assert.assertEquals(invoker2, mismatchInvokers.get(0));
    }

    /**
     * 测试apache invoker命中0.0.1版本实例的情况
     */
    @Test
    public void testApacheV1() {
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("0.0.1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("0.0.2");
        invokers.add(invoker2);
        List<Object> matchInvokers = RuleStrategyHandler.INSTANCE.getFlowMatchInvokers("foo", invokers, rule);
        Assert.assertEquals(100, routes.get(0).getWeight().intValue());
        Assert.assertEquals(1, matchInvokers.size());
        Assert.assertEquals(invoker1, matchInvokers.get(0));
    }

    /**
     * 测试apache invoker命中0.0.1版本实例的情况
     */
    @Test
    public void testApacheV1ByRequest() {
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("0.0.1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("0.0.2");
        invokers.add(invoker2);
        List<Object> matchInvokers = RuleStrategyHandler.INSTANCE
                .getMatchInvokersByRequest("foo", invokers,
                        Collections.singletonMap(RouterConstant.META_VERSION_KEY, "0.0.1"));
        Assert.assertEquals(100, routes.get(0).getWeight().intValue());
        Assert.assertEquals(1, matchInvokers.size());
        Assert.assertEquals(invoker1, matchInvokers.get(0));
    }

    /**
     * 测试apache invoker未命中0.0.1版本实例的情况
     */
    @Test
    public void testApacheMismatch() {
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("0.0.1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("0.0.2");
        invokers.add(invoker2);
        routes.get(0).setWeight(0);

        // 测试匹配上路由，没有随机到实例的情况
        List<Object> matchInvokers = RuleStrategyHandler.INSTANCE.getFlowMatchInvokers("foo", invokers, rule);
        Assert.assertEquals(1, matchInvokers.size());
        Assert.assertEquals(invoker2, matchInvokers.get(0));

        // 测试没有匹配上路由，选取不匹配标签的实例的情况
        List<Map<String, String>> tags = new ArrayList<>();
        tags.add(routes.get(0).getTags());
        List<Object> mismatchInvoker = RuleStrategyHandler.INSTANCE.getMismatchInvokers("foo", invokers, tags, true);
        Assert.assertEquals(1, mismatchInvoker.size());
        Assert.assertEquals(invoker2, mismatchInvoker.get(0));
    }

    /**
     * rule中route有选中tag，但是没有符合版本的实例，invoker命中fallback版本实例的情况
     */
    @Test
    public void testAlibabaV1Fallback() {
        setFallbackRoute();
        List<Object> invokers = new ArrayList<>();
        AlibabaInvoker<Object> invoker1 = new AlibabaInvoker<>("0.0.3");
        invokers.add(invoker1);
        AlibabaInvoker<Object> invoker2 = new AlibabaInvoker<>("0.0.4");
        invokers.add(invoker2);

        // Route随机命中route1，但是没有0.0.1版本实例；命中fallback，返回fallback实例信息
        List<Object> matchInvokers = RuleStrategyHandler.INSTANCE.getFlowMatchInvokers("foo", invokers, rule);
        Assert.assertEquals(100, routes.get(0).getWeight().intValue());
        Assert.assertEquals(1, matchInvokers.size());
        Assert.assertEquals(invoker1, matchInvokers.get(0));
    }

    private void setFallbackRoute() {
        List<Route> fallback = new ArrayList<>();
        Map<String, String> tags = new HashMap<>();
        tags.put(RouterConstant.META_VERSION_KEY, "0.0.3");
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
    public void testApacheV1NotMathRouteFallback() {
        setFallbackRoute();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("0.0.2");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("0.0.4");
        invokers.add(invoker2);

        // Route随机命中route1，但是没有0.0.1版本实例，fallback也未命中tag实例，所以返回全部实例信息
        List<Object> matchInvokers = RuleStrategyHandler.INSTANCE.getFlowMatchInvokers("foo", invokers, rule);
        Assert.assertEquals(2, matchInvokers.size());
    }

    /**
     * rule中设置routes但权重计算未命中，invoker命中fallback版本实例的情况
     */
    @Test
    public void testApacheV1MathFallback() {
        setFallbackRoute();
        routes.get(0).setWeight(0);
        routes.get(1).setWeight(0);

        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("0.0.2");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("0.0.3");
        invokers.add(invoker2);

        // Route计算权重均未命中tag，fallback权重计算命中tag，返回fallback规则命中实例信息
        List<Object> matchInvokers = RuleStrategyHandler.INSTANCE.getFlowMatchInvokers("foo", invokers, rule);
        Assert.assertEquals(1, matchInvokers.size());
        Assert.assertEquals(invoker2, matchInvokers.get(0));
    }

    /**
     * rule中设置routes但权重计算未命中，同时fallback也未命中实例，invoker返回未设置规则版本号版本实例的情况
     */
    @Test
    public void testApacheV1BothNotMathFallbackRoute() {
        setFallbackRoute();
        routes.get(0).setWeight(0);
        routes.get(1).setWeight(0);

        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("0.0.1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("0.0.4");
        invokers.add(invoker2);

        // Route计算权重均未命中tag，fallback权重计算也未命中tag，返回route中未设置tag实例信息
        List<Object> matchInvokers = RuleStrategyHandler.INSTANCE.getFlowMatchInvokers("foo", invokers, rule);
        Assert.assertEquals(1, matchInvokers.size());
        Assert.assertEquals(invoker2, matchInvokers.get(0));
    }
}