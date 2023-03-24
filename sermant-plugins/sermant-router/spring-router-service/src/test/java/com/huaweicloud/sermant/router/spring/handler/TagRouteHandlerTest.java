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

import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.spring.RuleInitializationUtils;
import com.huaweicloud.sermant.router.spring.TestDefaultServiceInstance;
import com.huaweicloud.sermant.router.spring.cache.AppCache;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TagRouteHandler单元测试
 *
 * @author lilai
 * @since 2023-02-27
 */
public class TagRouteHandlerTest {
    private static TagRouteHandler tagRouteHandler;

    /**
     * UT执行前进行mock
     */
    @BeforeClass
    public static void before() {
        tagRouteHandler = new TagRouteHandler();
    }

    /**
     * 测试getTargetInstancesByRules方法
     */
    @Test
    public void testGetTargetInstancesByTagRules() {
        RuleInitializationUtils.initTagMatchRule();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("group", "red");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试getTargetInstancesByRules方法
     */
    @Test
    public void testGetTargetInstancesByConsumerTagRules() {
        RuleInitializationUtils.initConsumerTagRules();
        List<Object> instances = new ArrayList<>();
        Map<String, String> metadata1 = new HashMap<>();
        metadata1.put("group", "red");
        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("group", "green");
        ServiceInstance instance1 = new TestDefaultServiceInstance(metadata1);
        instances.add(instance1);
        ServiceInstance instance2 = new TestDefaultServiceInstance(metadata2);
        instances.add(instance2);
        AppCache.INSTANCE.setMetadata(metadata1);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance1, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试getTargetInstancesByRules方法，同时配置服务维度规则和全局维度规则
     */
    @Test
    public void testGetTargetInstancesByTagRulesWithGlobalRules() {
        RuleInitializationUtils.initGlobalAndServiceTagMatchRules();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("group", "red");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetGlobalRule(Collections.emptyList());
    }

    /**
     * 测试getTargetInstancesByRules方法，配置全局维度规则
     */
    @Test
    public void testGetTargetInstancesByGlobalRules() {
        RuleInitializationUtils.initGlobalTagMatchRules();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("group", "red");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetGlobalRule(Collections.emptyList());
    }

    /**
     * 测试rule规则如下：
     * given: match为精确匹配az1，未设置policy，下游provider有az1实例
     * when: route至az1
     * then: 有能匹配到az1下游provider实例
     */
    @Test
    public void testGetTargetInstancesByTagRulesWithoutPolicySceneOne() {
        RuleInitializationUtils.initAZTagMatchRule();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az1");
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az2");
        instances.add(instance1);
        instances.add(instance2);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("zone", "az1");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance1, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given：match为精确匹配az1，未设置policy，下游provider无az1实例
     * when：route至az1
     * then：不能匹配到下游provider实例
     */
    @Test
    public void testGetTargetInstancesByTagRulesWithoutPolicySceneTwo() {
        RuleInitializationUtils.initAZTagMatchRule();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az2");
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az3");
        instances.add(instance1);
        instances.add(instance2);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("zone", "az1");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(0, targetInstances.size());
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given：match为精确匹配az1，设置policy的triggerThreshold为60，下游provider有az1实例
     * when：route至az1
     * then：能匹配到az1下游provider实例
     */
    @Test
    public void testGetTargetInstancesByTagRulesWithPolicySceneOne() {
        RuleInitializationUtils.initAZTagMatchTriggerThresholdPolicyRule();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az1");
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az2");
        ServiceInstance instance3 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", "az1");
        instances.add(instance1);
        instances.add(instance2);
        instances.add(instance3);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("zone", "az1");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(2, targetInstances.size());
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given: match为精确匹配az1，设置policy的triggerThreshold为60，下游provider有/无az1实例
     * when: route至az1，但是触发了阈值规则：az1可用实例数/ALL实例数小于triggerThreshold
     * then: 返回所有实例
     */
    @Test
    public void testGetTargetInstancesByTagRulesWithPolicySceneTwo() {
        RuleInitializationUtils.initAZTagMatchTriggerThresholdPolicyRule();
        // 场景一：下游provider有符合要求的实例
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az1");
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az2");
        instances.add(instance1);
        instances.add(instance2);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("zone", "az1");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(2, targetInstances.size());

        // 场景二：下游provider无符合要求的实例
        List<Object> instances2 = new ArrayList<>();
        ServiceInstance instance3 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az3");
        ServiceInstance instance4 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az4");
        instances2.add(instance3);
        instances2.add(instance4);
        metadata.put("zone", "az1");
        AppCache.INSTANCE.setMetadata(metadata);
        targetInstances = tagRouteHandler.handle("foo", instances2,
                new RequestData(null, null, null));
        Assert.assertEquals(2, targetInstances.size());

        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given: match为精确匹配az1，设置policy的triggerThreshold为50，minAllInstances为4，下游provider有az1实例
     * when: route至az1，minAllInstances小于5，az1实例/ALL实例占比大于triggerThreshold
     * then: 能匹配到az1的下游provider实例
     */
    @Test
    public void testGetTargetInstancesByTagRulesWithPolicySceneThree() {
        RuleInitializationUtils.initAZTagMatchTriggerThresholdMinAllInstancesPolicyRule();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az1");
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az2");
        ServiceInstance instance3 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", "az1");
        ServiceInstance instance4 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", "az2");
        ServiceInstance instance5 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.2", "az1");
        instances.add(instance1);
        instances.add(instance2);
        instances.add(instance3);
        instances.add(instance4);
        instances.add(instance5);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("zone", "az1");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(3, targetInstances.size());
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given：match为精确匹配az1，设置policy的triggerThreshold为50，minAllInstances为4，下游provider有az1实例
     * when：route至az1，minAllInstances小于5，az1实例/ALL实例占比小于triggerThreshold
     * then：返回所有下游provider实例
     */
    @Test
    public void testGetTargetInstancesByTagRulesWithPolicySceneFour() {
        RuleInitializationUtils.initAZTagMatchTriggerThresholdMinAllInstancesPolicyRule();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az1");
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az2");
        ServiceInstance instance3 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", "az1");
        ServiceInstance instance4 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", "az2");
        ServiceInstance instance5 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.2", "az2");
        instances.add(instance1);
        instances.add(instance2);
        instances.add(instance3);
        instances.add(instance4);
        instances.add(instance5);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("zone", "az1");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(5, targetInstances.size());
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试rule规则如下：
     * given：match为精确匹配az1，设置policy的triggerThreshold为50，minAllInstances为4，下游provider有az1实例
     * when：route至az1，minAllInstances大于3，az1实例/ALL实例占比小于或者大于triggerThreshold
     * then：返回下游az1的provider实例
     */
    @Test
    public void testGetTargetInstancesByTagRulesWithPolicySceneFive() {
        RuleInitializationUtils.initAZTagMatchTriggerThresholdMinAllInstancesPolicyRule();
        // 场景一: az1实例/ALL实例占比大于triggerThreshold
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az1");
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az2");
        ServiceInstance instance3 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", "az1");
        instances.add(instance1);
        instances.add(instance2);
        instances.add(instance3);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("zone", "az1");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = tagRouteHandler.handle("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(2, targetInstances.size());

        // 场景二: az1实例/ALL实例占比小于triggerThreshold
        List<Object> instances2 = new ArrayList<>();
        ServiceInstance instance4 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az1");
        ServiceInstance instance5 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az2");
        ServiceInstance instance6 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", "az2");
        instances2.add(instance4);
        instances2.add(instance5);
        instances2.add(instance6);
        targetInstances = tagRouteHandler.handle("foo", instances2,
                new RequestData(null, null, null));
        Assert.assertEquals(1, targetInstances.size());

        List<Object> instances3 = new ArrayList<>();
        ServiceInstance instance7 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az3");
        ServiceInstance instance8 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", "az2");
        ServiceInstance instance9 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", "az2");
        instances3.add(instance7);
        instances3.add(instance8);
        instances3.add(instance9);
        targetInstances = tagRouteHandler.handle("foo", instances3,
                new RequestData(null, null, null));
        Assert.assertEquals(0, targetInstances.size());

        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }
}
