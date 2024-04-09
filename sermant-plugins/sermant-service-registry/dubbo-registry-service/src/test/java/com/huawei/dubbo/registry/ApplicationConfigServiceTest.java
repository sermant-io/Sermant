/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.dubbo.registry;

import com.huawei.dubbo.registry.cache.DubboCache;
import com.huawei.dubbo.registry.service.ApplicationConfigService;
import com.huawei.dubbo.registry.service.ApplicationConfigServiceImpl;

import com.alibaba.dubbo.config.ApplicationConfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test ApplicationConfigServiceImpl
 *
 * @author provenceee
 * @since 2022-02-14
 */
public class ApplicationConfigServiceTest {
    private static final String FOO = "foo";

    private final ApplicationConfigService service;

    /**
     * Constructor
     */
    public ApplicationConfigServiceTest() {
        service = new ApplicationConfigServiceImpl();
    }

    /**
     * Test Alibaba ApplicationConfig
     *
     * @see com.alibaba.dubbo.config.ApplicationConfig
     */
    @Test
    public void testAlibabaApplicationConfig() {
        // Clear the cache
        DubboCache.INSTANCE.setServiceName(null);
        ApplicationConfig alibabaConfig = new ApplicationConfig();

        // Test invalid app name
        service.getName(alibabaConfig);
        Assertions.assertNull(DubboCache.INSTANCE.getServiceName());

        // Test valid app names
        alibabaConfig.setName(FOO);
        service.getName(alibabaConfig);
        Assertions.assertEquals(FOO, DubboCache.INSTANCE.getServiceName());
    }

    /**
     * Test Apache ApplicationConfig
     *
     * @see org.apache.dubbo.config.ApplicationConfig
     */
    @Test
    public void testApacheApplicationConfig() {
        // Clear the cache
        DubboCache.INSTANCE.setServiceName(null);
        org.apache.dubbo.config.ApplicationConfig apacheConfig = new org.apache.dubbo.config.ApplicationConfig();

        // Test invalid app name
        service.getName(apacheConfig);
        Assertions.assertNull(DubboCache.INSTANCE.getServiceName());

        // Test valid app names
        apacheConfig.setName(FOO);
        service.getName(apacheConfig);
        Assertions.assertEquals(FOO, DubboCache.INSTANCE.getServiceName());
    }
}
