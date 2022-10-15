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

package com.huawei.discovery.entity;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

/**
 * hash测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class HashedServiceInstanceTest {
    @Test
    public void test() {
        String host = "localhost";
        String ip = "127.0.0.1";
        int port = 9090;
        final Map<String, String> meta = Collections.singletonMap("zone", "region-A");
        String serviceName = "test";
        final DefaultServiceInstance instance = new DefaultServiceInstance(host, ip, port, meta,
                serviceName);
        final Map<String, String> newMeta = Collections.singletonMap("zone", "region-B");
        final DefaultServiceInstance instance2 = new DefaultServiceInstance(host, ip, port, newMeta,
                serviceName);
        Assert.assertEquals(instance, instance2);
    }
}
