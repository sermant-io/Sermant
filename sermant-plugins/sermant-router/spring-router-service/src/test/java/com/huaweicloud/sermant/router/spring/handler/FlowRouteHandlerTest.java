/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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
 * FlowRouteHandler Unit tests
 *
 * @author lilai
 * @since 2023-02-27
 */
public class FlowRouteHandlerTest {
    private static FlowRouteHandler flowRouteHandler;

    private static RouterConfig config;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    /**
     * Perform mock before the UT is executed
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
     * Release the mock object after the UT is executed
     */
    @AfterClass
    public static void after() {
        mockPluginConfigManager.close();
    }

    /**
     * Reset
     */
    @Before
    public void reset() {
        config.setUseRequestRouter(false);
    }

    /**
     * Test the getTargetInstancesByRules method
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
     * Test the getTargetInstancesByRequest method (without label)
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

        // When the test is not tags
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(instances, targetInstances);
    }

    /**
     * Test the getTargetInstancesByRequest method (labeled)
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

        // Match the foo:bar2 instance
        header.clear();
        header.put("foo", Collections.singletonList("bar2"));
        header.put("foo1", Collections.singletonList("bar2"));
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));

        // The instance that matches version 1.0.0 is matched
        header.clear();
        header.put("version", Collections.singletonList("1.0.0"));
        targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance1, targetInstances.get(0));
    }

    /**
     * When testing getTargetInstancesByRequest method mismatch
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

        // Bar does not match: If the bar1 instance does not match, the instance without the bar label is matched
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        List<Object> targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(2, targetInstances.size());
        Assert.assertFalse(targetInstances.contains(instance2));

        // If the bar: bar1 instance does not match, the instance without the bar tag is preferentially matched,
        // and if there is no instance without the bar tag, an empty list is returned
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

        // If the version: 1.0.3 instance does not match, all versions of the instance are returned
        header.clear();
        header.put("version", Collections.singletonList("1.0.3"));
        targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(3, targetInstances.size());

        // If no header is passed, the instance without a tag is matched
        header.clear();
        targetInstances = flowRouteHandler.handle("foo", instances, new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance3, targetInstances.get(0));

        // If no header is passed, the instance without a label is matched first,
        // and if there are no instances without a label, all instances are returned
        header.clear();
        targetInstances = flowRouteHandler.handle("foo", sameInstances, new RequestData(header, null, null));
        Assert.assertEquals(sameInstances, targetInstances);
    }

    /**
     * Test handle method when there is only one downstream
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
     * When tested to match a downstream service instance
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
     * Test the getTargetInstancesByRules method and configure both the service dimension rule and the global dimension rule
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
     * Test the getTargetInstancesByRules method to configure global dimension rules
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
