/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.service.cache;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.huawei.registry.config.GraceConfig;
import com.huawei.registry.service.register.NacosServiceInstance;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

/**
 * Service caching tests
 *
 * @author chengyouling
 * @since 2022-11-17
 */
public class ServiceCacheTest {
    private static final int SERVICE_ID_SIZE = 2;

    private static final int INSTANCES_SIZE = 1;

    /**
     * Test address caching
     */
    @Test
    public void testAddress() {
        List<NacosServiceInstance> instances = new ArrayList<>();
        NacosServiceInstance instance1 = new NacosServiceInstance();
        instance1.setServiceId("test1");
        instances.add(instance1);
        ServiceCache.setInstances("test1", instances);
        ServiceCache.setServiceIds(Arrays.asList("test1", "test2"));
        assertEquals(INSTANCES_SIZE, ServiceCache.getInstances("test1").size());
        assertEquals(SERVICE_ID_SIZE, ServiceCache.getServiceIds().size());
    }
}
