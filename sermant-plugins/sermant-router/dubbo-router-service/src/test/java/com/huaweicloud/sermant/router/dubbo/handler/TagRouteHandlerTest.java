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
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.dubbo.ApacheInvoker;
import com.huaweicloud.sermant.router.dubbo.RuleInitializationUtils;
import com.huaweicloud.sermant.router.dubbo.cache.DubboCache;
import com.huaweicloud.sermant.router.dubbo.service.AbstractDirectoryServiceTest;

import org.apache.dubbo.rpc.Invocation;
import org.junit.AfterClass;
import org.junit.Assert;
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
 * TagRouteHandler单元测试
 *
 * @author lilai
 * @since 2023-02-28
 */
public class TagRouteHandlerTest {
    private static TagRouteHandler tagRouteHandler;

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
        tagRouteHandler = new TagRouteHandler();
    }

    /**
     * UT执行后释放mock对象
     */
    @AfterClass
    public static void after() {
        mockPluginConfigManager.close();
    }

    /**
     * 测试getGetTargetInstances方法
     */
    @Test
    public void testGetTargetInvokerByTagRules() {
        // 初始化路由规则
        RuleInitializationUtils.initTagMatchRule();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        invocation.setAttachment("bar", "bar1");
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "red");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.put(RouterConstant.PARAMETERS_KEY_PREFIX + "group", "red");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                ".huaweicloud.foo.FooTest")
            , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试getGetTargetInstances方法，配置全局维度规则
     */
    @Test
    public void testGetTargetInvokerByGlobalRules() {
        // 初始化路由规则
        RuleInitializationUtils.initGlobalTagMatchRules();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        invocation.setAttachment("bar", "bar1");
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "red");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.put(RouterConstant.PARAMETERS_KEY_PREFIX + "group", "red");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                ".huaweicloud.foo.FooTest")
            , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetGlobalRule(Collections.emptyList());
    }

    /**
     * 测试getGetTargetInstances方法，同时配置服务维度规则和全局维度规则
     */
    @Test
    public void testGetTargetInvokerByTagRulesWithGlobalRules() {
        // 初始化路由规则
        RuleInitializationUtils.initGlobalAndServiceTagMatchRules();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1");
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        invocation.setAttachment("bar", "bar1");
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "red");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.put(RouterConstant.PARAMETERS_KEY_PREFIX + "group", "red");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                ".huaweicloud.foo.FooTest")
            , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker2, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetGlobalRule(Collections.emptyList());
    }

    /**
     * 测试getGetTargetInstances方法
     */
    @Test
    public void testGetTargetInvokerByConsumerTagRules() {
        // 初始化路由规则
        RuleInitializationUtils.initConsumerTagRules();
        List<Object> invokers = new ArrayList<>();
        Map<String, String> parameters1 = new HashMap<>();
        parameters1.put(RouterConstant.PARAMETERS_KEY_PREFIX + "group", "red");
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0", parameters1);
        invokers.add(invoker1);
        Map<String, String> parameters2 = new HashMap<>();
        parameters1.put(RouterConstant.PARAMETERS_KEY_PREFIX + "group", "green");
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1", parameters2);
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        invocation.setAttachment("bar", "bar1");
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("side", "consumer");
        queryMap.put("group", "red");
        queryMap.put("version", "0.0.1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.put(RouterConstant.PARAMETERS_KEY_PREFIX + "group", "red");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com" +
                ".huaweicloud.foo.FooTest")
            , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker1, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }
}
