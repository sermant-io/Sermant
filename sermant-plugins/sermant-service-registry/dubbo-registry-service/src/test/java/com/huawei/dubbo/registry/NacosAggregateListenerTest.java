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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.huawei.dubbo.registry.listener.NacosAggregateListener;

/**
 * 测试NacosAggregateListener
 *
 * @author chengyouling
 * @since 2022-11-29
 */
public class NacosAggregateListenerTest {

    /**
     * 测试equal
     */
    @Test
    public void testEqual() {
        Instance instance = new Instance();
        instance.setIp("127.0.0.1");
        List<Instance> instances = new ArrayList<>();
        instances.add(instance);
        Object object = new Object();
        NacosAggregateListener listener1 = new NacosAggregateListener(object);
        listener1.saveAndAggregateAllInstances("providers:interface:DEFAULT");
        NacosAggregateListener listener2 = new NacosAggregateListener(object);
        listener2.saveAndAggregateAllInstances("providers:interface:DEFAULT");
        Assertions.assertEquals(listener1.equals(listener2), true);
        Assertions.assertEquals(listener1.getNotifyListener(), object);
        Assertions.assertEquals(listener1.getServiceNames().size(), 1);
    }
}
