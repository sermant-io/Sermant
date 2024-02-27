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

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.event.config.EventConfig;
import com.huaweicloud.sermant.router.common.cache.DubboCache;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.dubbo.ApacheInvoker;
import com.huaweicloud.sermant.router.dubbo.RuleInitializationUtils;
import com.huaweicloud.sermant.router.dubbo.service.AbstractDirectoryServiceTest;

import org.apache.dubbo.rpc.Invocation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collections;


/**
 * TagRouteHandler单元测试
 *
 * @author lilai
 * @since 2023-02-28
 */
public class TagRouteHandlerTest {
    private static TagRouteHandler tagRouteHandler;

    private static EventConfig config;

    private static MockedStatic<ConfigManager> mockConfigManager;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        config = new EventConfig();
        config.setEnable(false);
        mockConfigManager = Mockito.mockStatic(ConfigManager.class);
        mockConfigManager.when(() -> ConfigManager.getConfig(EventConfig.class)).thenReturn(config);
        tagRouteHandler = new TagRouteHandler();
    }

    /**
     * UT执行后释放资源
     */
    @AfterClass
    public static void after(){
        mockConfigManager.close();
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

    /**
     * 测试rule规则如下：
     * given: match为精确匹配az1，未设置policy，下游provider有az1实例
     * when: route至az1
     * then: 有能匹配到az1下游provider实例
     */
    @Test
    public void testGetTargetInvokerByTagRulesWithoutPolicySceneOne() {
        // 初始化路由规则
        RuleInitializationUtils.initAZTagMatchRule();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0", "az1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1", "az2");
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("zone", "az1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.putIfAbsent(RouterConstant.META_ZONE_KEY, "az1");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());
        Assert.assertEquals(invoker1, targetInvokers.get(0));
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given：match为精确匹配az1，未设置policy，下游provider无az1实例
     * when：route至az1
     * then：不能匹配到下游provider实例，返回所有实例
     */
    @Test
    public void testGetTargetInvokerByTagRulesWithoutPolicySceneTwo() {
        // 初始化路由规则
        RuleInitializationUtils.initAZTagMatchRule();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0", "az2");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1", "az3");
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("zone", "az1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.putIfAbsent(RouterConstant.META_ZONE_KEY, "az1");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(2, targetInvokers.size());
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given：match为精确匹配az1，设置policy的triggerThreshold为60，下游provider有az1实例
     * when：route至az1
     * then：能匹配到az1下游provider实例
     */
    @Test
    public void testGetTargetInvokerByTagRulesWithPolicySceneOne() {
        // 初始化路由规则
        RuleInitializationUtils.initAZTagMatchTriggerThresholdPolicyRule();
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0", "az1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1", "az2");
        invokers.add(invoker2);
        ApacheInvoker<Object> invoker3 = new ApacheInvoker<>("1.0.1", "az1");
        invokers.add(invoker3);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("zone", "az1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.putIfAbsent(RouterConstant.META_ZONE_KEY, "az1");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(2, targetInvokers.size());
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given: match为精确匹配az1，设置policy的triggerThreshold为60，下游provider有/无az1实例
     * when: route至az1，但是触发了阈值规则：az1可用实例数/ALL实例数小于triggerThreshold
     * then: 返回所有实例
     */
    @Test
    public void testGetTargetInvokerByTagRulesWithPolicySceneTwo() {
        // 初始化路由规则
        RuleInitializationUtils.initAZTagMatchTriggerThresholdPolicyRule();
        // 场景一：下游provider有符合要求的实例
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0", "az1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.1", "az2");
        invokers.add(invoker2);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("zone", "az1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.putIfAbsent(RouterConstant.META_ZONE_KEY, "az1");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(2, targetInvokers.size());

        // 场景二：下游provider无符合要求的实例
        List<Object> invokers2 = new ArrayList<>();
        ApacheInvoker<Object> invoker3 = new ApacheInvoker<>("1.0.0", "az2");
        invokers2.add(invoker3);
        ApacheInvoker<Object> invoker4 = new ApacheInvoker<>("1.0.1", "az3");
        invokers2.add(invoker4);
        targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud.foo.FooTest")
                , invokers2, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(2, targetInvokers.size());

        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given: match为精确匹配az1，设置policy的triggerThreshold为50，minAllInstances为4，下游provider有az1实例
     * when: route至az1，minAllInstances小于5，az1实例/ALL实例占比大于triggerThreshold
     * then: 能匹配到az1的下游provider实例
     */
    @Test
    public void testGetTargetInvokerByTagRulesWithPolicySceneThree() {
        // 初始化路由规则
        RuleInitializationUtils.initAZTagMatchTriggerThresholdMinAllInstancesPolicyRule();
        // 场景一：下游provider有符合要求的实例
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0", "az1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.0", "az2");
        invokers.add(invoker2);
        ApacheInvoker<Object> invoker3 = new ApacheInvoker<>("1.0.1", "az1");
        invokers.add(invoker3);
        ApacheInvoker<Object> invoker4 = new ApacheInvoker<>("1.0.1", "az2");
        invokers.add(invoker4);
        ApacheInvoker<Object> invoker5 = new ApacheInvoker<>("1.0.2", "az1");
        invokers.add(invoker5);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("zone", "az1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.putIfAbsent(RouterConstant.META_ZONE_KEY, "az1");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(3, targetInvokers.size());
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given：match为精确匹配az1，设置policy的triggerThreshold为50，minAllInstances为4，下游provider有az1实例
     * when：route至az1，minAllInstances小于5，az1实例/ALL实例占比小于triggerThreshold
     * then：返回所有下游provider实例
     */
    @Test
    public void testGetTargetInvokerByTagRulesWithPolicySceneFour() {
        // 初始化路由规则
        RuleInitializationUtils.initAZTagMatchTriggerThresholdMinAllInstancesPolicyRule();
        // 场景一：下游provider有符合要求的实例
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0", "az1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.0", "az2");
        invokers.add(invoker2);
        ApacheInvoker<Object> invoker3 = new ApacheInvoker<>("1.0.1", "az1");
        invokers.add(invoker3);
        ApacheInvoker<Object> invoker4 = new ApacheInvoker<>("1.0.1", "az2");
        invokers.add(invoker4);
        ApacheInvoker<Object> invoker5 = new ApacheInvoker<>("1.0.2", "az2");
        invokers.add(invoker5);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("zone", "az1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.putIfAbsent(RouterConstant.META_ZONE_KEY, "az1");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(5, targetInvokers.size());
        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given：match为精确匹配az1，设置policy的triggerThreshold为50，minAllInstances为4，下游provider有az1实例
     * when：route至az1，minAllInstances大于3，az1实例/ALL实例占比小于或者大于triggerThreshold
     * then：返回下游az1的provider实例
     */
    @Test
    public void testGetTargetInvokerByTagRulesWithPolicySceneFive() {
        // 初始化路由规则
        RuleInitializationUtils.initAZTagMatchTriggerThresholdMinAllInstancesPolicyRule();
        // 场景一: az1实例/ALL实例占比大于triggerThreshold
        List<Object> invokers = new ArrayList<>();
        ApacheInvoker<Object> invoker1 = new ApacheInvoker<>("1.0.0", "az1");
        invokers.add(invoker1);
        ApacheInvoker<Object> invoker2 = new ApacheInvoker<>("1.0.0", "az2");
        invokers.add(invoker2);
        ApacheInvoker<Object> invoker3 = new ApacheInvoker<>("1.0.0", "az1");
        invokers.add(invoker3);
        Invocation invocation = new AbstractDirectoryServiceTest.ApacheInvocation();
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("zone", "az1");
        queryMap.put("interface", "com.huaweicloud.foo.FooTest");
        Map<String, String> parameters = new HashMap<>();
        parameters.putIfAbsent(RouterConstant.META_ZONE_KEY, "az1");
        DubboCache.INSTANCE.setParameters(parameters);
        DubboCache.INSTANCE.putApplication("com.huaweicloud.foo.FooTest", "foo");
        List<Object> targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud.foo.FooTest")
                , invokers, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(2, targetInvokers.size());

        // 场景二: az1实例/ALL实例占比小于triggerThreshold
        List<Object> invokers2 = new ArrayList<>();
        ApacheInvoker<Object> invoker4 = new ApacheInvoker<>("1.0.0", "az1");
        invokers2.add(invoker4);
        ApacheInvoker<Object> invoker5 = new ApacheInvoker<>("1.0.0", "az2");
        invokers2.add(invoker5);
        ApacheInvoker<Object> invoker6 = new ApacheInvoker<>("1.0.1", "az2");
        invokers2.add(invoker6);
        targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud.foo.FooTest")
                , invokers2, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(1, targetInvokers.size());

        // 未匹配上则返回所有实例
        List<Object> invokers3 = new ArrayList<>();
        ApacheInvoker<Object> invoker7 = new ApacheInvoker<>("1.0.0", "az3");
        invokers3.add(invoker7);
        ApacheInvoker<Object> invoker8 = new ApacheInvoker<>("1.0.0", "az2");
        invokers3.add(invoker8);
        ApacheInvoker<Object> invoker9 = new ApacheInvoker<>("1.0.1", "az2");
        invokers3.add(invoker9);
        targetInvokers = (List<Object>) tagRouteHandler.handle(DubboCache.INSTANCE.getApplication("com.huaweicloud.foo.FooTest")
                , invokers3, invocation, queryMap, "com.huaweicloud.foo.FooTest");
        Assert.assertEquals(3, targetInvokers.size());

        ConfigCache.getLabel(RouterConstant.DUBBO_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }
}
