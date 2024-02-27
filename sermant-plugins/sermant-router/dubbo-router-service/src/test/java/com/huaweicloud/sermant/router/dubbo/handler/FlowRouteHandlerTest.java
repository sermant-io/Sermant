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
 * FlowRouteHandler单元测试
 *
 * @author lilai
 * @since 2023-02-28
 */
public class FlowRouteHandlerTest {
    private static FlowRouteHandler flowRouteHandler;

    private static MockedStatic<PluginConfigManager> mockPluginConfigManager;

    private static RouterConfig config;

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

    public FlowRouteHandlerTest() {
    }

    @Before
    public void clear() {
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "");
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
        config.setUseRequestRouter(false);
    }

    /**
     * 测试getGetTargetInvokers方法
     */
    @Test
    public void testGetTargetInvokersByFlowRules() {
        // 初始化路由规则
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
     * 测试getGetTargetInvokers方法，配置全局维度规则
     */
    @Test
    public void testGetTargetInvokersByGlobalRules() {
        // 初始化路由规则
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
     * 测试getGetTargetInvokers方法，同时配置服务维度规则和全局维度规则
     */
    @Test
    public void testGetTargetInvokersByFlowRulesWithGlobalRules() {
        // 初始化路由规则
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
     * 测试getTargetInvokersByRequest方法
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

        // 匹配foo: bar2实例
        invocation.setAttachment("foo", "bar2");
        invocation.setAttachment("foo1", "bar2");
        List<Object> targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                        ".huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));

        // 匹配1.0.0版本实例
        invocation.getObjectAttachments().clear();
        invocation.setAttachment("version", "1.0.0");
        targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud" +
                        ".foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker1, targetInvokers.get(0));
    }

    /**
     * 测试getTargetInvokersByRequest方法不匹配时
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

        // 不匹配bar: bar1实例时，匹配没有bar标签的实例
        invocation.setAttachment("bar", "bar1");
        List<Object> targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                        ".huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(2, targetInvokers.size());
        Assert.assertFalse(targetInvokers.contains(invoker2));

        // 不匹配bar: bar1实例时，优先匹配没有bar标签的实例，如果没有无bar标签的实例，则返回空列表
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

        // 不匹配version: 1.0.3实例时，返回所有版本的实例
        invocation.getObjectAttachments().clear();
        invocation.setAttachment("version", "1.0.3");
        targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud" +
                        ".foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(3, targetInvokers.size());

        // 不传入attachment时，匹配无标签实例
        invocation.getObjectAttachments().clear();
        targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud" +
                        ".foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker3, targetInvokers.get(0));

        // 不传入attachment时，优先匹配无标签实例，没有无标签实例时，返回全部实例
        invocation.getObjectAttachments().clear();
        targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud" +
                        ".foo.FooTest")
                , sameInvokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(sameInvokers, targetInvokers);
    }

    /**
     * 测试没有命中路由时
     */
    @Test
    public void testGetMissMatchInvokers() {
        // 初始化路由规则
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

        // 测试无tags时
        List<Object> targetInvokers = (List<Object>) flowRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                        ".huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(invokers, targetInvokers);
    }
}
