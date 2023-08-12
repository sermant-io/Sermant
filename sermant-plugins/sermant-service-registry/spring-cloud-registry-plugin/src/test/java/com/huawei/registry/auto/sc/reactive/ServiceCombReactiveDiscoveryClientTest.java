/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.registry.auto.sc.reactive;

import com.huawei.registry.entity.FixedResult;
import com.huawei.registry.entity.MicroServiceInstance;
import com.huawei.registry.services.RegisterCenterService;

import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * client测试
 *
 * @author zhouss
 * @since 2022-09-06
 */
public class ServiceCombReactiveDiscoveryClientTest {
    private static final String SERVICE_NAME = "test";
    private static final List<String> SERVICES = Arrays.asList("a", "b", "c");
    private static final List<MicroServiceInstance> INSTANCES = Arrays.asList(buildInstance(), buildInstance());
    private final ServiceCombReactiveDiscoveryClient client = new ServiceCombReactiveDiscoveryClient();

    @Before
    public void setUp() {
        ReflectUtils.setFieldValue(client, "registerCenterService", new TestRegisterService());
    }

    @Test
    public void getInstances() {
        final Flux<ServiceInstance> test = client.getInstances(SERVICE_NAME);
        final List<ServiceInstance> block = test.collectList().block();
        Assert.assertNotNull(block);
        Assert.assertEquals(block.size(), INSTANCES.size());
        Assert.assertEquals(block.get(0).getServiceId(), SERVICE_NAME);
    }

    @Test
    public void getServices() {
        final Flux<String> services = client.getServices();
        final List<String> block = services.collectList().block();
        Assert.assertNotNull(block);
        Assert.assertEquals(block.size(), SERVICES.size());
        for (int i = 0; i < block.size(); i++) {
            Assert.assertEquals(block.get(i), SERVICES.get(i));
        }
    }

    /**
     * 构建实例
     *
     * @return MicroServiceInstance
     */
    public static MicroServiceInstance buildInstance() {
        return new MicroServiceInstance() {
            @Override
            public String getServiceName() {
                return SERVICE_NAME;
            }

            @Override
            public String getHost() {
                return null;
            }

            @Override
            public String getIp() {
                return null;
            }

            @Override
            public int getPort() {
                return 0;
            }

            @Override
            public String getServiceId() {
                return null;
            }

            @Override
            public String getInstanceId() {
                return null;
            }

            @Override
            public Map<String, String> getMetadata() {
                return null;
            }

            @Override
            public boolean isSecure() {
                return false;
            }
        };
    }

    /**
     * 测试用
     *
     * @since 2022-09-06
     */
    public static class TestRegisterService implements RegisterCenterService {

        @Override
        public void register(FixedResult result) {

        }

        @Override
        public void unRegister() {

        }

        @Override
        public List<MicroServiceInstance> getServerList(String serviceId) {
            return INSTANCES;
        }

        @Override
        public List<String> getServices() {
            return SERVICES;
        }

        @Override
        public String getRegisterCenterStatus() {
            return null;
        }

        @Override
        public String getInstanceStatus() {
            return null;
        }

        @Override
        public void updateInstanceStatus(String status) {

        }
    }
}
