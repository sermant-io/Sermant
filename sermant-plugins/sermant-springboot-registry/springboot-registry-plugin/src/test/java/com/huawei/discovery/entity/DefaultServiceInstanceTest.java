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

import com.huawei.discovery.entity.ServiceInstance.Status;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

/**
 * 实例简单测试
 *
 * @author zhouss
 * @since 2022-10-12
 */
public class DefaultServiceInstanceTest {
    @Test
    public void test() {
        String host = "localhost";
        String ip = "127.0.0.1";
        int port = 9090;
        final Map<String, String> meta = Collections.singletonMap("zone", "region-A");
        String serviceName = "test";
        final DefaultServiceInstance instance = new DefaultServiceInstance(host, ip, port, meta,
                serviceName);
        Assert.assertEquals(instance.getId(), ip + ":" + port);
        Assert.assertEquals(instance.getHost(), host);
        Assert.assertEquals(instance.getIp(), ip);
        Assert.assertEquals(instance.getPort(), port);
        Assert.assertEquals(instance.getMetadata(), meta);
        Assert.assertEquals(instance.getStatus(), Status.UP.name());
        Assert.assertEquals(instance.getServiceName(), serviceName);
    }
}
