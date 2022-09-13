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
import com.huaweicloud.sermant.router.config.label.entity.Route;
import com.huaweicloud.sermant.router.dubbo.AlibabaInvoker;
import com.huaweicloud.sermant.router.dubbo.ApacheInvoker;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
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

    /**
     * 构造方法
     */
    public RuleStrategyHandlerTest() {
        routes = new ArrayList<>();
        Map<String, String> tags1 = new HashMap<>();
        tags1.put(RouterConstant.DUBBO_VERSION_KEY, "0.0.1");
        Route route1 = new Route();
        route1.setTags(tags1);
        route1.setWeight(100);
        routes.add(route1);
        Map<String, String> tags2 = new HashMap<>();
        tags2.put(RouterConstant.DUBBO_VERSION_KEY, "0.0.2");
        Route route2 = new Route();
        route2.setTags(tags2);
        route2.setWeight(100);
        routes.add(route2);
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
        List<Object> targetInvoker = RuleStrategyHandler.INSTANCE.getTargetInvoker(routes, invokers);
        Assert.assertEquals(100, routes.get(0).getWeight().intValue());
        Assert.assertEquals(1, targetInvoker.size());
        Assert.assertEquals(invoker1, targetInvoker.get(0));
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
        List<Object> targetInvoker = RuleStrategyHandler.INSTANCE.getTargetInvoker(routes, invokers);
        Assert.assertEquals(1, targetInvoker.size());
        Assert.assertEquals(invoker2, targetInvoker.get(0));

        // 测试没有匹配上路由，选取不匹配标签的实例的情况
        List<Map<String, String>> tags = new ArrayList<>();
        tags.add(routes.get(0).getTags());
        List<Object> missMatchInvoker = RuleStrategyHandler.INSTANCE.getMissMatchInstances(tags, invokers);
        Assert.assertEquals(1, missMatchInvoker.size());
        Assert.assertEquals(invoker2, missMatchInvoker.get(0));
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
        List<Object> targetInvoker = RuleStrategyHandler.INSTANCE.getTargetInvoker(routes, invokers);
        Assert.assertEquals(100, routes.get(0).getWeight().intValue());
        Assert.assertEquals(1, targetInvoker.size());
        Assert.assertEquals(invoker1, targetInvoker.get(0));
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
        List<Object> targetInvoker = RuleStrategyHandler.INSTANCE.getTargetInvoker(routes, invokers);
        Assert.assertEquals(1, targetInvoker.size());
        Assert.assertEquals(invoker2, targetInvoker.get(0));

        // 测试没有匹配上路由，选取不匹配标签的实例的情况
        List<Map<String, String>> tags = new ArrayList<>();
        tags.add(routes.get(0).getTags());
        List<Object> missMatchInvoker = RuleStrategyHandler.INSTANCE.getMissMatchInstances(tags, invokers);
        Assert.assertEquals(1, missMatchInvoker.size());
        Assert.assertEquals(invoker2, missMatchInvoker.get(0));
    }
}