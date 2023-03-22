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

package com.huaweicloud.sermant.router.spring.service;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.router.common.config.RouterConfig;
import com.huaweicloud.sermant.router.common.constants.RouterConstant;
import com.huaweicloud.sermant.router.common.request.RequestData;
import com.huaweicloud.sermant.router.config.cache.ConfigCache;
import com.huaweicloud.sermant.router.spring.RuleInitializationUtils;
import com.huaweicloud.sermant.router.spring.TestDefaultServiceInstance;
import com.huaweicloud.sermant.router.spring.cache.AppCache;

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
 * 测试LoadBalancerServiceImpl
 *
 * @author provenceee
 * @since 2022-09-13
 */
public class LoadBalancerServiceTest {
    private static LoadBalancerService loadBalancerService;

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
        loadBalancerService = new LoadBalancerServiceImpl();
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
     * 测试getGetTargetInstances方法(flow匹配规则)
     */
    @Test
    public void testGetGetTargetInstancesByFlowMatchRules() {
        RuleInitializationUtils.initFlowMatchRule();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        List<Object> targetInstances = loadBalancerService.getTargetInstances("foo", instances,
                new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试getGetTargetInstances方法(tag匹配规则)
     */
    @Test
    public void testGetTargetInstancesByTagMatchRules() {
        RuleInitializationUtils.initTagMatchRule();
        List<Object> instances = new ArrayList<>();
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0");
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1");
        instances.add(instance2);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("group", "red");
        AppCache.INSTANCE.setMetadata(metadata);
        List<Object> targetInstances = loadBalancerService.getTargetInstances("foo", instances,
                new RequestData(null, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }

    /**
     * 测试getGetTargetInstances方法(同时下发flow和tag匹配规则)
     */
    @Test
    public void testGetTargetInstancesByAllRules() {
        RuleInitializationUtils.initAllRules();
        List<Object> instances = new ArrayList<>();
        Map<String, String> metadata1 = new HashMap<>();
        metadata1.put("group", "red");
        Map<String, String> metadata2 = new HashMap<>();
        metadata2.put("group", "green");
        ServiceInstance instance1 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.0", metadata1);
        instances.add(instance1);
        ServiceInstance instance2 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", metadata1);
        instances.add(instance2);
        ServiceInstance instance3 = TestDefaultServiceInstance.getTestDefaultServiceInstance("1.0.1", metadata2);
        instances.add(instance3);
        AppCache.INSTANCE.setMetadata(metadata1);
        Map<String, List<String>> header = new HashMap<>();
        header.put("bar", Collections.singletonList("bar1"));
        List<Object> targetInstances = loadBalancerService.getTargetInstances("foo", instances,
                new RequestData(header, null, null));
        Assert.assertEquals(1, targetInstances.size());
        Assert.assertEquals(instance2, targetInstances.get(0));
        ConfigCache.getLabel(RouterConstant.SPRING_CACHE_NAME).resetRouteRule(Collections.emptyMap());
    }
}