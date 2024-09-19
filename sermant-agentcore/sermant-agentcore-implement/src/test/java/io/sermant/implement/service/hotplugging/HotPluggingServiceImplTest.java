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

package io.sermant.implement.service.hotplugging;

import io.sermant.core.config.ConfigManager;
import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.dynamicconfig.DynamicConfigService;
import io.sermant.core.service.dynamicconfig.config.DynamicConfig;
import io.sermant.core.utils.AesUtil;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.ReflectUtils;

import io.sermant.implement.operation.converter.YamlConverterImpl;
import io.sermant.implement.service.dynamicconfig.nacos.NacosDynamicConfigService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Optional;

/**
 * Unit Tests for HotPluggingServiceImpl
 *
 * @author zhp
 * @since 2024-09-02
 */
public class HotPluggingServiceImplTest {
    private static final String LISTENERS_KEY = "listeners";

    private MockedStatic<ConfigManager> dynamicConfigMockedStatic;

    private MockedStatic<OperationManager> operationManagerMockedStatic;

    private MockedStatic<ServiceManager> serviceManagerMockedStatic;

    private NacosDynamicConfigService nacosDynamicConfigService;

    private final DynamicConfig dynamicConfig = new DynamicConfig();

    private final ServiceMeta serviceMeta = new ServiceMeta();

    @Before
    public void setUp() {
        dynamicConfig.setEnableAuth(true);
        dynamicConfig.setServerAddress("127.0.0.1:8848");
        dynamicConfig.setTimeoutValue(30000);
        Optional<String> optional = AesUtil.generateKey();
        dynamicConfig.setPrivateKey(optional.orElse(""));
        dynamicConfig.setUserName("nacos");
        dynamicConfig.setPassword(AesUtil.encrypt(optional.get(), "nacos").orElse(""));
        serviceMeta.setProject("testProject2");
        serviceMeta.setApplication("testApplication");
        serviceMeta.setEnvironment("testEnvironment");
        serviceMeta.setCustomLabel("testCustomLabel");
        serviceMeta.setCustomLabelValue("testCustomLabelValue");
        dynamicConfigMockedStatic = Mockito.mockStatic(ConfigManager.class);
        dynamicConfigMockedStatic.when(() -> ConfigManager.getConfig(DynamicConfig.class))
                .thenReturn(dynamicConfig);
        dynamicConfigMockedStatic.when(() -> ConfigManager.getConfig(ServiceMeta.class))
                .thenReturn(serviceMeta);
        nacosDynamicConfigService = new NacosDynamicConfigService();
        nacosDynamicConfigService.start();
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(DynamicConfigService.class))
                .thenReturn(nacosDynamicConfigService);
        operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class))
                .thenReturn(new YamlConverterImpl());
    }

    @Test
    public void testStartAndStop() {
        HotPluggingServiceImpl service = new HotPluggingServiceImpl();
        service.start();
        Optional<Object> optional = ReflectUtils.getFieldValue(nacosDynamicConfigService, LISTENERS_KEY);
        Assert.assertTrue(optional.isPresent());
        Assert.assertFalse(CollectionUtils.isEmpty((Collection<?>) optional.get()));
        service.stop();
        Assert.assertTrue(CollectionUtils.isEmpty((Collection<?>) optional.get()));
    }

    @After
    public void closeMock() {
        if (operationManagerMockedStatic != null) {
            operationManagerMockedStatic.close();
        }
        if (dynamicConfigMockedStatic != null) {
            dynamicConfigMockedStatic.close();
        }
        if (serviceManagerMockedStatic != null) {
            serviceManagerMockedStatic.close();
        }
    }
}