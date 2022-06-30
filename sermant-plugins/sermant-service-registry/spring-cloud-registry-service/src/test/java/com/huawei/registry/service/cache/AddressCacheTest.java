/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

import com.huawei.registry.config.GraceConfig;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * 地址缓存测试
 *
 * @author zhouss
 * @since 2022-06-30
 */
public class AddressCacheTest {
    private static final int SIZE = 3;

    /**
     * 测试地址缓存
     */
    @Test
    public void testAddress() {
        Mockito.mockStatic(PluginConfigManager.class)
                .when(() -> PluginConfigManager.getPluginConfig(GraceConfig.class))
                .thenReturn(new GraceConfig());
        AddressCache.INSTANCE.addAddress("localhost:1200");
        AddressCache.INSTANCE.addAddress("localhost:1300");
        AddressCache.INSTANCE.addAddress("localhost:1400");
        assertEquals(SIZE, AddressCache.INSTANCE.getAddressSet().size());
    }
}
