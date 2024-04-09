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

package com.huaweicloud.sermant.router.dubbo.handler;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.dubbo.ApacheInvoker;
import com.huaweicloud.sermant.router.dubbo.RuleInitializationUtils;
import com.huaweicloud.sermant.router.dubbo.service.AbstractDirectoryServiceTest;

import org.apache.dubbo.rpc.Invocation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FlowRouteHandler unit testing
 *
 * @author lilai
 * @since 2023-02-28
 */
public class FlowRouteHandlerTest {
    private static FlowRouteHandler flowRouteHandler;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private static RouterConfig config;

    /**
     * Mock before UT execution
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
     * Release all mock objects after UT execution
     */
    @AfterClass
    public static void after() {
        mockPluginConfigManager.close();
    }

    public FlowRouteHandlerTest() {
    }

    @Before
    public void clear() {
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "");
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
        config.setUseRequestRouter(false);
    }

    /**
     * Test the getGetTargetInvokers method
     */
    @Test
    public void testGetTargetInvokersByFlowRules() {
        // initialize the routing rule
        RuleInitializationUtils.initFlowMatchRule();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        invocation.setAttachment("bar", "bar1");
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "fooGroup");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                        ".huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * Test the getGetTargetInvokers method and configure global dimension rules
     */
    @Test
    public void testGetTargetInvokersByGlobalRules() {
        // initialize the routing rule
        RuleInitializationUtils.initGlobalFlowMatchRules();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        invocation.setAttachment("bar", "bar1");
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "fooGroup");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                        ".huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetGlobalRule(Collections.emptyList());
    }

    /**
     * Test the getTargetInvokers method and configure both the service dimension rule and the global dimension rule
     */
    @Test
    public void testGetTargetInvokersByFlowRulesWithGlobalRules() {
        // initialize the routing rule
        RuleInitializationUtils.initGlobalAndServiceFlowMatchRules();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        invocation.setAttachment("bar", "bar1");
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "fooGroup");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                        ".huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetGlobalRule(Collections.emptyList());
    }

    /**
     * Test the getTargetInvokersByRequest method
     */
    @Test
    public void testGetTargetInvokersByRequest() {
        config.setRequestTags(Arrays.asList("foo", "bar", "version"));
        config.setUseRequestRouter(true);
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0",
                Collections.singletonMap(RouterConstant.PARAMETERS_KEY_PREFIX + "bar", "bar1"));
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1",
                Collections.singletonMap(RouterConstant.PARAMETERS_KEY_PREFIX + "foo", "bar2"));
        invokers.add(invoker2);
        ApacheInvoker<Object> invoker3 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker3);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "fooGroup");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");

        // Matching instances with foo: bar2
        invocation.setAttachment("foo", "bar2");
        invocation.setAttachment("foo1", "bar2");
        List<Object> targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                        ".huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));

        // Matching instances with version 1.0.0
        invocation.getObjectAttachments().clear();
        invocation.setAttachment("version", "1.0.0");
        targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud" +
                        ".foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker1, targetInvokers.get(0));
    }

    /**
     * When testing getTargetInvokersByRequest method mismatch
     */
    @Test
    public void testGetTargetInvokersByRequestWithMismatch() {
        config.setUseRequestRouter(true);
        config.setRequestTags(Arrays.asList("foo", "bar", "version"));
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0",
                Collections.singletonMap(RouterConstant.PARAMETERS_KEY_PREFIX + "foo", "bar1"));
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1",
                Collections.singletonMap(RouterConstant.PARAMETERS_KEY_PREFIX + "bar", "bar2"));
        invokers.add(invoker2);
        ApacheInvoker<Object> invoker3 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker3);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "fooGroup");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");

        // When not matching the bar: bar1 instance, match instances without bar labels
        invocation.setAttachment("bar", "bar1");
        List<Object> targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                        ".huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(2, targetInvokers.size());
        Assert.assertFalse(targetInvokers.contains(invoker2));

        // When not matching instance bar1, prioritize matching instances without bar labels.
        // If there are no instances without bar labels, return an empty list
        List<Object> sameInvokers = new ArrayList<>();
        ApacheInvoker<Object> sameInvoker1 = new ApacheInvoker<>("1.0.0",
                Collections.singletonMap(RouterConstant.PARAMETERS_KEY_PREFIX + "bar", "bar3"));
        sameInvokers.add(sameInvoker1);
        ApacheInvoker<Object> sameInvoker2 = new ApacheInvoker<>("1.0.1",
                Collections.singletonMap(RouterConstant.PARAMETERS_KEY_PREFIX + "bar", "bar2"));
        sameInvokers.add(sameInvoker2);
        invocation.getObjectAttachments().clear();
        invocation.setAttachment("bar", "bar1");
        targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud" +
                        ".foo.FooTest")
                , sameInvokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(0, targetInvokers.size());

        // When the version does not match the 1.0.3 instance, return all instances of the version
        invocation.getObjectAttachments().clear();
        invocation.setAttachment("version", "1.0.3");
        targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud" +
                        ".foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(3, targetInvokers.size());

        // Match unlabeled instances when no attachment is passed in
        invocation.getObjectAttachments().clear();
        targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud" +
                        ".foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker3, targetInvokers.get(0));

        // When no attachment is passed in, priority is given to matching unlabeled instances. When there are no
        // unlabeled instances, all instances are returned
        invocation.getObjectAttachments().clear();
        targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud" +
                        ".foo.FooTest")
                , sameInvokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(sameInvokers, targetInvokers);
    }

    /**
     * test when there are no routes hit
     */
    @Test
    public void testGetMissMatchInvokers() {
        // initialize the routing rule
        RuleInitializationUtils.initFlowMatchRule();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        invocation.setAttachment("bar", "bar2");
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "fooGroup");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                        ".huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker1, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    @Test
    public void testGetTargetInstancesByRequestWithNoTags() {
        config.setRequestTags(null);
        config.setUseRequestRouter(true);
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0",
                Collections.singletonMap(RouterConstant.PARAMETERS_KEY_PREFIX + "bar", "bar1"));
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1",
                Collections.singletonMap(RouterConstant.PARAMETERS_KEY_PREFIX + "foo", "bar2"));
        invokers.add(invoker2);
        ApacheInvoker<Object> invoker3 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker3);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "fooGroup");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");

        // when the test is not tags
        List<Object> targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                        ".huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(invokers, targetInvokers);
    }
}
