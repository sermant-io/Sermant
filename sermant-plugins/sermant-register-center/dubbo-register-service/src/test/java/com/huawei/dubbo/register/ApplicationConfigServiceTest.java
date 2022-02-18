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

package com.huawei.dubbo.register;

import com.huawei.dubbo.register.cache.DubboCache;
import com.huawei.dubbo.register.service.ApplicationConfigService;
import com.huawei.dubbo.register.service.ApplicationConfigServiceImpl;
import com.huawei.register.config.RegisterConfig;

import com.alibaba.dubbo.config.ApplicationConfig;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试ApplicationConfigServiceImpl
 *
 * @author provenceee
 * @since 2022/2/14
 */
public class ApplicationConfigServiceTest {
    private static final String FOO = "foo";

    private static final String VERSION_KEY = "gray.version";

    private final ApplicationConfigService service;

    private final RegisterConfig registerConfig;

    public ApplicationConfigServiceTest() throws IllegalAccessException, NoSuchFieldException {
        service = new ApplicationConfigServiceImpl();
        registerConfig = new RegisterConfig();
        Field field = service.getClass().getDeclaredField("config");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(service, registerConfig);
    }

    /**
     * 测试Alibaba ApplicationConfig
     *
     * @see com.alibaba.dubbo.config.ApplicationConfig
     */
    @Test
    public void testAlibabaApplicationConfig() {
        // 清空缓存
        DubboCache.INSTANCE.setServiceName(null);
        ApplicationConfig alibabaConfig = new ApplicationConfig();

        // 测试无效应用名
        service.getName(alibabaConfig);
        Assert.assertNull(DubboCache.INSTANCE.getServiceName());
        Assert.assertNull(alibabaConfig.getParameters());

        // 测试有效应用名，parameters为null
        alibabaConfig.setName(FOO);
        service.getName(alibabaConfig);
        Assert.assertEquals(FOO, DubboCache.INSTANCE.getServiceName());
        Assert.assertNotNull(alibabaConfig.getParameters());
        Assert.assertEquals(registerConfig.getVersion(), alibabaConfig.getParameters().get(VERSION_KEY));

        // 清空parameters
        alibabaConfig.setParameters(null);
        Assert.assertNull(alibabaConfig.getParameters());

        // 测试parameters不为null
        Map<String, String> map = new HashMap<>();
        map.put(FOO, "bar");
        alibabaConfig.setParameters(map);
        service.getName(alibabaConfig);
        Assert.assertNotNull(alibabaConfig.getParameters());
        Assert.assertEquals(2, alibabaConfig.getParameters().size());
        Assert.assertEquals(registerConfig.getVersion(), alibabaConfig.getParameters().get(VERSION_KEY));
    }

    /**
     * 测试Apache ApplicationConfig
     *
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    @Test
    public void testApacheApplicationConfig() {
        // 清空缓存
        DubboCache.INSTANCE.setServiceName(null);
        org.apache.dubbo.config.ApplicationConfig apacheConfig = new org.apache.dubbo.config.ApplicationConfig();

        // 测试无效应用名
        service.getName(apacheConfig);
        Assert.assertNull(DubboCache.INSTANCE.getServiceName());
        Assert.assertNull(apacheConfig.getParameters());

        // 测试有效应用名，parameters为null
        apacheConfig.setName(FOO);
        service.getName(apacheConfig);
        Assert.assertEquals(FOO, DubboCache.INSTANCE.getServiceName());
        Assert.assertNotNull(apacheConfig.getParameters());
        Assert.assertEquals(registerConfig.getVersion(), apacheConfig.getParameters().get(VERSION_KEY));

        // 清空parameters
        apacheConfig.setParameters(null);
        Assert.assertNull(apacheConfig.getParameters());

        // 测试parameters不为null
        Map<String, String> map = new HashMap<>();
        map.put(FOO, "bar");
        apacheConfig.setParameters(map);
        service.getName(apacheConfig);
        Assert.assertNotNull(apacheConfig.getParameters());
        Assert.assertEquals(2, apacheConfig.getParameters().size());
        Assert.assertEquals(registerConfig.getVersion(), apacheConfig.getParameters().get(VERSION_KEY));
    }
}