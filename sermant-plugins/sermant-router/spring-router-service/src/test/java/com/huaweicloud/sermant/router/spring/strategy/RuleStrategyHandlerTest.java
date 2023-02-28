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

import com.huaweicloud.sermant.router.config.entity.Route;
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

    /**
     * 构造方法
     */
    public RuleStrategyHandlerTest() {
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
        List<Object> matchInvoker = RuleStrategyHandler.INSTANCE.getMatchInstances("foo", instances, routes);
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
        List<Object> matchInstances = RuleStrategyHandler.INSTANCE.getMatchInstances("foo", instances, routes);
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
}