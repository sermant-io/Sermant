/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.retry;

import com.huawei.discovery.entity.ServiceInstance;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * 测试Context
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class InvokerContextTest {
    @Test
    public void test() {
        final Exception exception = new Exception("error");
        final ServiceInstance serviceInstance = Mockito.mock(ServiceInstance.class);
        final InvokerContext invokerContext = new InvokerContext();
        invokerContext.setServiceInstance(serviceInstance);
        invokerContext.setEx(exception);
        Assert.assertEquals(exception, invokerContext.getEx());
        Assert.assertEquals(serviceInstance, invokerContext.getServiceInstance());
    }
}
