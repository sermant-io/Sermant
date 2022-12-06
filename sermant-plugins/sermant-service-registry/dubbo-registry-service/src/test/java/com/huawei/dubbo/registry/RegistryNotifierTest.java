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

package com.huawei.dubbo.registry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.huawei.dubbo.registry.service.RegistryNotifier;

/**
 * 测试RegistryNotifier
 *
 * @author chengyouling
 * @since 2022-11-29
 */
public class RegistryNotifierTest {

    /**
     * 测试构建NamingService
     *
     * @throws NoSuchMethodException 找不到方法
     * @throws InvocationTargetException InvocationTargetException
     * @throws IllegalAccessException IllegalAccessException
     */
    @Test
    public void testNotify() throws NoSuchFieldException {
        RegistryNotifier notifier = new RegistryNotifier(5000) {
            @Override
            protected void doNotify(Object rawAddresses) {

            }
        };
        Instance instance = new Instance();
        instance.setIp("127.0.0.1");
        instance.setPort(8202);
        Map<String, String> metadata = new HashMap<>();
        metadata.put("protocol", "dubbo");
        metadata.put("path", "com.huawei.dubbo.registry.service.RegistryService");
        instance.setMetadata(metadata);
        List<Instance> instances = new LinkedList<>();
        instances.add(instance);
        notifier.notify(instances);
        Field field = RegistryNotifier.class.getDeclaredField("SCHEDULER");
        field.setAccessible(true);
        Assertions.assertNotNull(field);
    }
}
