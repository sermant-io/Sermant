/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.register.service.client;

import org.apache.servicecomb.service.center.client.ServiceCenterOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * sc客户端测试
 *
 * @author zhouss
 * @since 2022-01-05
 */
public class ScClientTest extends BaseTest {
    ScClient scClient;
    Registration registration;

    @Before
    public void before() {
        scClient = new ScClient();
        registration = new Registration() {
            @Override
            public String getServiceId() {
                return "test";
            }

            @Override
            public String getHost() {
                return "localhost";
            }

            @Override
            public int getPort() {
                return 8080;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public URI getUri() {
                return URI.create("http://localhost:8080");
            }

            @Override
            public Map<String, String> getMetadata() {
                return new HashMap<String, String>();
            }
        };
    }

    @Test
    public void buildMicro() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        scClient.start();
        final ServiceCenterOperation rawClient = scClient.getRawClient();
        Assert.assertNotNull(rawClient);
        final Method buildMicroService = scClient.getClass().getDeclaredMethod("buildMicroService", Registration.class);
        buildMicroService.setAccessible(true);
        final Object serviceResult = buildMicroService.invoke(scClient, registration);
        Assert.assertNotNull(serviceResult);
        final Method buildMicroServiceInstance = scClient.getClass().getDeclaredMethod("buildMicroServiceInstance", Registration.class, String.class);
        buildMicroServiceInstance.setAccessible(true);
        final Object instanceResult = buildMicroServiceInstance.invoke(scClient, registration, registration.getServiceId());
        Assert.assertNotNull(instanceResult);
        scClient.stop();
    }
}
