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

package com.huaweicloud.sermant.router.spring.strategy.mapper;

import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.zookeeper.discovery.ZookeeperInstance;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServer;

import java.util.Collections;
import java.util.Map;

/**
 * 测试ZookeeperMetadataMapper
 *
 * @author provenceee
 * @since 2022-09-30
 */
public class ZookeeperMetadataMapperTest {
    /**
     * 测试ZookeeperServer获取元数据方法
     *
     * @throws Exception 异常
     */
    @Test
    public void testApply() throws Exception {
        Map<String, String> map = Collections.singletonMap("foo", "bar");
        ZookeeperInstance instance = new ZookeeperInstance("id", "name", map);
        ServiceInstanceBuilder<ZookeeperInstance> builder = ServiceInstance.builder();
        ServiceInstance<ZookeeperInstance> serviceInstance =
            builder.address("localhost").port(80).name("name").payload(instance).build();
        ZookeeperServer server = new ZookeeperServer(serviceInstance);
        ZookeeperMetadataMapper mapper = new ZookeeperMetadataMapper();
        Map<String, String> metadata = mapper.apply(server);
        Assert.assertNotNull(metadata);
        Assert.assertEquals("bar", metadata.get("foo"));
    }
}