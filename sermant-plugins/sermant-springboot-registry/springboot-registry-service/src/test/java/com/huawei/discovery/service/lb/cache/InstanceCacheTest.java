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

package com.huawei.discovery.service.lb.cache;

import com.huawei.discovery.entity.DefaultServiceInstance;
import com.huawei.discovery.entity.ServiceInstance;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 测试实例缓存实体
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class InstanceCacheTest {
    @Test
    public void test() {
        String serviceName = "test";
        final List<ServiceInstance> instances = new ArrayList<>();
        instances.add(new DefaultServiceInstance("localhost", "127.0.0.1", 8080, Collections.emptyMap(),
                serviceName));
        final InstanceCache instanceCache = new InstanceCache(serviceName, instances);
        Assert.assertEquals(serviceName, instanceCache.getServiceName());
        Assert.assertEquals(instanceCache.getInstances(), instances);
    }
}
