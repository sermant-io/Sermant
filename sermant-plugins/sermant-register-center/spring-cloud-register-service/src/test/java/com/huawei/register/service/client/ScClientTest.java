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

import com.huawei.register.context.RegisterContext;

import org.apache.servicecomb.service.center.client.ServiceCenterOperation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * sc客户端测试
 *
 * @author zhouss
 * @since 2022-01-05
 */
public class ScClientTest extends BaseTest {
    private static final int PORT = 8080;
    private ScClient scClient;

    /**
     * 前置
     */
    @Before
    public void before() {
        scClient = new ScClient();
        RegisterContext.INSTANCE.getClientInfo().setServiceName("test");
        RegisterContext.INSTANCE.getClientInfo().setServiceId("test");
        RegisterContext.INSTANCE.getClientInfo().setHost("localhost");
        RegisterContext.INSTANCE.getClientInfo().setPort(PORT);
        RegisterContext.INSTANCE.getClientInfo().setMeta(new HashMap<>());
    }

    /**
     * 构建实例信息
     *
     * @throws NoSuchMethodException 无该方法抛出
     * @throws InvocationTargetException 调用异常时抛出
     * @throws IllegalAccessException 无法拿到目标对象抛出
     */
    @Test
    public void buildMicro() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        scClient.init();
        final ServiceCenterOperation rawClient = scClient.getRawClient();
        Assert.assertNotNull(rawClient);
        final Method buildMicroService = scClient.getClass().getDeclaredMethod("buildMicroService");
        buildMicroService.setAccessible(true);
        final Object serviceResult = buildMicroService.invoke(scClient);
        Assert.assertNotNull(serviceResult);
        final Method buildMicroServiceInstance = scClient.getClass()
            .getDeclaredMethod("buildMicroServiceInstance", String.class);
        buildMicroServiceInstance.setAccessible(true);
        final Object instanceResult = buildMicroServiceInstance
            .invoke(scClient, RegisterContext.INSTANCE.getClientInfo().getServiceId());
        Assert.assertNotNull(instanceResult);
    }
}
