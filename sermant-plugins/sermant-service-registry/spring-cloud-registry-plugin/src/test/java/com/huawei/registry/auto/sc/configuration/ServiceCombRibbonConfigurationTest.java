/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.auto.sc.configuration;

import static org.junit.Assert.*;

import com.huaweicloud.sermant.core.utils.ReflectUtils;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

/**
 * Test obtaining serverList
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class ServiceCombRibbonConfigurationTest {
    /**
     * Get ServerList Test
     */
    @Test
    public void serverList() {
        final IClientConfig clientConfig = Mockito.mock(IClientConfig.class);
        final ServiceCombRibbonConfiguration serviceCombRibbonConfiguration = new ServiceCombRibbonConfiguration();
        final ServerList<?> serverList = serviceCombRibbonConfiguration.serverList(clientConfig);
        final Optional<Object> configOptional = ReflectUtils.getFieldValue(serverList, "clientConfig");
        Assert.assertTrue(configOptional.isPresent());
        Assert.assertTrue(configOptional.get() instanceof IClientConfig);
        Assert.assertEquals(clientConfig, configOptional.get());
    }
}
