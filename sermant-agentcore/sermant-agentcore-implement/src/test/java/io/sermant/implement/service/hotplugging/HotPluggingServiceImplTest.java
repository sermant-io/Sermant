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

import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.dynamicconfig.DynamicConfigService;
import io.sermant.core.utils.CollectionUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.implement.operation.converter.YamlConverterImpl;
import io.sermant.implement.service.dynamicconfig.nacos.NacosBaseTest;

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
public class HotPluggingServiceImplTest extends NacosBaseTest {
    private static final MockedStatic<OperationManager> operationManagerMockedStatic = Mockito.mockStatic(OperationManager.class);

    private static final String LISTENERS_KEY = "listeners";

    @Before
    public void setUp() {
        operationManagerMockedStatic.when(() -> OperationManager.getOperation(YamlConverter.class))
                .thenReturn(new YamlConverterImpl());
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(DynamicConfigService.class))
                .thenReturn(nacosDynamicConfigService);
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
        operationManagerMockedStatic.close();
        if (serviceManagerMockedStatic != null) {
            serviceManagerMockedStatic.close();
        }
    }
}