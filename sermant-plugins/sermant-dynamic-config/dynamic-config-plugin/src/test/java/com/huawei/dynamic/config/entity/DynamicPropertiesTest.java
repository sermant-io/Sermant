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

package com.huawei.dynamic.config.entity;

import com.huawei.dynamic.config.init.DynamicConfigInitializer;

import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * attribute initialization test
 *
 * @author zhouss
 * @since 2022-09-05
 */
public class DynamicPropertiesTest {
    /**
     * test the initialization method
     */
    @Test
    public void testInit() {
        final DynamicProperties dynamicProperties = new DynamicProperties();
        String serviceName = "testService";
        ReflectUtils.setFieldValue(dynamicProperties, "serviceName", serviceName);
        try(final MockedStatic<ServiceManager> serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class)) {
            final AtomicBoolean executed = new AtomicBoolean();
            final DynamicConfigInitializer dynamicConfigInitializer = new DynamicConfigInitializer() {
                @Override
                public void doStart() {
                    executed.set(true);
                }
            };
            serviceManagerMockedStatic.when(() -> ServiceManager.getService(DynamicConfigInitializer.class))
                    .thenReturn(dynamicConfigInitializer);
            dynamicProperties.init();
            Assert.assertTrue(executed.get());
            Assert.assertEquals(ClientMeta.INSTANCE.getServiceName(), serviceName);
        }
    }
}
