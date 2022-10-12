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

package com.huawei.discovery.service.retry.policy;

import com.huawei.discovery.entity.ServiceInstance;
import com.huawei.discovery.service.lb.DiscoveryManager;
import com.huawei.discovery.service.lb.discovery.zk.ZkService34;
import com.huawei.discovery.service.lb.rule.BaseTest;
import com.huawei.discovery.service.lb.utils.CommonUtils;

import com.huaweicloud.sermant.core.plugin.service.PluginServiceManager;
import com.huaweicloud.sermant.core.utils.ReflectUtils;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 轮询重试策略测试
 *
 * @author zhouss
 * @since 2022-10-10
 */
public class RoundRobinRetryPolicyTest extends BaseTest {
    @Mock
    private ZkService34 zkService34;

    @Override
    public void setUp() {
        super.setUp();
        MockitoAnnotations.openMocks(this);
        pluginServiceManagerMockedStatic.when(() -> PluginServiceManager.getPluginService(ZkService34.class))
                .thenReturn(zkService34);
        start();
    }

    @Override
    public void tearDown() {
        super.tearDown();
        // 重置状态
        final Optional<Object> isStarted = ReflectUtils.getFieldValue(DiscoveryManager.INSTANCE, "isStarted");
        Assert.assertTrue(isStarted.isPresent() && isStarted.get() instanceof AtomicBoolean);
        ((AtomicBoolean) isStarted.get()).set(false);
    }

    private void start() {
        DiscoveryManager.INSTANCE.start();
        Mockito.verify(zkService34, Mockito.times(1)).init();
    }

    @Test
    public void select() {
        String serviceName = "test";
        final ServiceInstance selectedInstance = CommonUtils.buildInstance(serviceName, 8989);
        final ServiceInstance nextInstance = CommonUtils.buildInstance(serviceName, 8888);
        final List<ServiceInstance> serviceInstances = Arrays.asList(selectedInstance, nextInstance);
        try {
            Mockito.when(zkService34.getInstances(serviceName)).thenReturn(serviceInstances);
        } catch (com.huawei.discovery.service.ex.QueryInstanceException e) {
            e.printStackTrace();
        }
        final RoundRobinRetryPolicy roundRobinRetryPolicy = new RoundRobinRetryPolicy();
        final Optional<ServiceInstance> select = roundRobinRetryPolicy.select(serviceName, selectedInstance);
        Assert.assertTrue(select.isPresent());
        Assert.assertEquals(select.get(), nextInstance);
    }
}
