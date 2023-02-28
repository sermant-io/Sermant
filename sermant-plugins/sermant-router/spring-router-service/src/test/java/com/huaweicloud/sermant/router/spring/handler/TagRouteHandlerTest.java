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
}
