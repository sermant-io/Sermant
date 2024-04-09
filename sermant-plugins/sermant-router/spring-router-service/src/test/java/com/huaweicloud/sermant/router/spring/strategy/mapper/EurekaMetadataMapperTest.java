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

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.Builder;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

/**
 * Test EurekaMetadataMapper
 *
 * @author provenceee
 * @since 2022-09-30
 */
public class EurekaMetadataMapperTest {
    /**
     * Test the EurekaMetadataMapper method to get metadata
     */
    @Test
    public void testApply() {
        Map<String, String> map = Collections.singletonMap("foo", "bar");
        InstanceInfo instanceInfo = Builder.newBuilder().setAppName("APP").setMetadata(map).build();
        DiscoveryEnabledServer server = new DiscoveryEnabledServer(instanceInfo, false);
        EurekaMetadataMapper mapper = new EurekaMetadataMapper();
        Map<String, String> metadata = mapper.apply(server);
        Assert.assertNotNull(metadata);
        Assert.assertEquals("bar", metadata.get("foo"));
    }
}