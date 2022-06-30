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

package com.huawei.registry.utils;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.config.SpringRegistryConstants;

import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;

/**
 * 测试设置zone
 *
 * @author zhouss
 * @since 2022-06-29
 */
public class ZoneUtilsTest {
    /**
     * 测试读取zone
     */
    @Test
    public void testSetZone() {
        final HashMap<String, String> meta = new HashMap<>(
                Collections.singletonMap(SpringRegistryConstants.LOAD_BALANCER_ZONE_META_KEY, "test"));
        ZoneUtils.setZone(meta);
        Assert.assertEquals(meta.get(SpringRegistryConstants.LOAD_BALANCER_ZONE_META_KEY), "test");
        final RegisterConfig registerConfig = new RegisterConfig();
        String zone = "registerZone";
        registerConfig.setZone(zone);
        Mockito.mockStatic(PluginConfigManager.class).when(() -> PluginConfigManager.getPluginConfig(RegisterConfig.class))
                .thenReturn(registerConfig);
        final HashMap<String, String> map = new HashMap<>();
        ZoneUtils.setZone(map);
        Assert.assertEquals(map.get(SpringRegistryConstants.LOAD_BALANCER_ZONE_META_KEY), zone);
    }
}
