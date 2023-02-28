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

package com.huaweicloud.sermant.router.spring.handler;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.spring.RuleInitializationUtils;
import com.huaweicloud.sermant.router.spring.TestDefaultServiceInstance;

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
 * FlowRouteHandler单元测试
 *
 * @author lilai
 * @since 2023-02-27
 */
public class FlowRouteHandlerTest {
    private static FlowRouteHandler flowRouteHandler;

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
        flowRouteHandler = new FlowRouteHandler();
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockPluginConfigManager.close();
    }

    /**
     * 重置
     */
    @Before
    public void reset() {
        config.setUseRequestRouter(false);
    }

    /**
     * 测试getTargetInstancesByRules方法
     */
    @Test
    public void testGetTargetInstancesByRules() {
        RuleInitializationUtils.initFlowMatchRule();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances,
            new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试getTargetInstancesByRequest方法(没有标签)
     */
    @Test
    public void testGetTargetInstancesByRequestWithNoTags() {
        config.setUseRequestRouter(true);
        config.setRequestTags(null);
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
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(instances, targetInstances);
    }

    /**
     * 测试getTargetInstancesByRequest方法(有标签)
     */
    @Test
    public void testGetTargetInstancesByRequestWithTags() {
        config.setUseRequestRouter(true);
        config.setRequestTags(Arrays.asList("foo", "bar", "version"));
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

        // 匹配foo: bar2实例
        header.clear();
        header.put("foo", Collections.singletonList("bar2"));
        header.put("foo1", Collections.singletonList("bar2"));
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));

        // 匹配1.0.0版本实例
        header.clear();
        header.put("version", Collections.singletonList("1.0.0"));
        targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
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
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
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
        targetInstances = flowRouteHandler.handle("foo", sameInstances, new RequestData(header, null, null));
        Assert.assertEquals(0, targetInstances.size());

        // 不匹配version: 1.0.3实例时，返回所有版本的实例
        header.clear();
        header.put("version", Collections.singletonList("1.0.3"));
        targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(3, targetInstances.size());

        // 不传入header时，匹配无标签实例
        header.clear();
        targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance3, targetInstances.get(0));

        // 不传入header时，优先匹配无标签实例，没有无标签实例时，返回全部实例
        header.clear();
        targetInstances = flowRouteHandler.handle("foo", sameInstances, new RequestData(header, null, null));
        Assert.assertEquals(sameInstances, targetInstances);
    }

    /**
     * 测试handle方法只有一个下游时
     */
    @Test
    public void testGetTargetInstancesWithOneInstance() {
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instances, targetInstances);
    }

    /**
     * 测试为匹配到下游服务实例时
     */
    @Test
    public void testGetMismatchInstances() {
        RuleInitializationUtils.initFlowMatchRule();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);

        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar2"));
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance1, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试getTargetInstancesByRules方法, 同时配置服务维度规则和全局维度规则
     */
    @Test
    public void testGetTargetInstancesByFlowRulesWithGlobalRules() {
        RuleInitializationUtils.initGlobalAndServiceFlowMatchRules();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances,
            new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetGlobalRule(Collections.emptyList());
    }

    /**
     * 测试getTargetInstancesByRules方法, 配置全局维度规则
     */
    @Test
    public void testGetTargetInstancesByGlobalRules() {
        RuleInitializationUtils.initGlobalFlowMatchRules();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances,
            new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetGlobalRule(Collections.emptyList());
    }
}
