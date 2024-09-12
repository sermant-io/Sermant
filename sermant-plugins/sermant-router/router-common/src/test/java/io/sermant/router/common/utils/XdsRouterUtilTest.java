/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.router.common.utils;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsServiceDiscovery;
import io.sermant.core.service.xds.entity.ServiceInstance;
import io.sermant.core.service.xds.entity.XdsLocality;
import io.sermant.core.utils.NetworkUtils;
import io.sermant.router.common.xds.TestServiceInstance;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * XdsRouterUtilTest
 *
 * @author daizhenyu
 * @since 2024-08-31
 **/
public class XdsRouterUtilTest {
    private static XdsServiceDiscovery serviceDiscovery;

    private static MockedStatic<ServiceManager> serviceManager;

    private static MockedStatic<NetworkUtils> networkUtils;

    private static MockedStatic<ConfigManager> configManager;

    @BeforeClass
    public static void setUp() throws Exception {
        XdsCoreService xdsCoreService = Mockito.mock(XdsCoreService.class);
        serviceManager = Mockito.mockStatic(ServiceManager.class);
        Mockito.when(ServiceManager.getService(XdsCoreService.class)).thenReturn(xdsCoreService);
        networkUtils = Mockito.mockStatic(NetworkUtils.class);
        Mockito.when(NetworkUtils.getKubernetesPodIp()).thenReturn("127.0.0.1");

        serviceDiscovery = Mockito.mock(XdsServiceDiscovery.class);
        Mockito.when(xdsCoreService.getXdsServiceDiscovery()).thenReturn(serviceDiscovery);
        Mockito.when(serviceDiscovery.getServiceInstance("serviceA"))
                .thenReturn(createServiceInstance4Service(Arrays.asList("127.0.0.1", "host", "localhost")));

        configManager = Mockito.mockStatic(ConfigManager.class);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        serviceManager.close();
        networkUtils.close();
        configManager.close();
    }

    @Test
    public void testGetLocalityInfoOfSelfService() {
        // not find matched service instance
        ServiceMeta meta = new ServiceMeta();
        meta.setService("serviceB");
        Mockito.when(ConfigManager.getConfig(ServiceMeta.class)).thenReturn(meta);
        Optional<XdsLocality> localityInfo = XdsRouterUtils.getLocalityInfoOfSelfService();
        Assert.assertFalse(localityInfo.isPresent());

        // find matched service instance
        meta.setService("serviceA");
        localityInfo = XdsRouterUtils.getLocalityInfoOfSelfService();
        Assert.assertTrue(localityInfo.isPresent());
        Assert.assertEquals("127.0.0.1", localityInfo.get().getRegion());
    }

    private static Set<ServiceInstance> createServiceInstance4Service(List<String> hosts) {
        Set<ServiceInstance> serviceInstances = new HashSet<>();
        for (String host : hosts) {
            TestServiceInstance serviceInstance = new TestServiceInstance();
            serviceInstance.setHost(host);
            Map<String, String> metaData = new HashMap<>();
            metaData.put("region", host);
            serviceInstance.setMetaData(metaData);
            serviceInstances.add(serviceInstance);
        }
        return serviceInstances;
    }
}