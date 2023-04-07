/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.service;

import com.huaweicloud.sermant.cache.InstanceCache;
import com.huaweicloud.sermant.config.RemovalConfig;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.entity.InstanceInfo;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 服务调用任务测试类
 *
 * @author zhp
 * @since 2023-02-21
 */
public class RequestDataCountTaskTest {
    private static final String KEY = "127.0.0.1:8080";

    private static final int REQ_NUM = 10;

    private static final int REQ_FAIL_NUM = 5;

    private static final double ERROR_RATE = 0.5;

    private static MockedStatic<ConfigManager> configManagerMockedStatic;

    private static MockedStatic<ServiceManager> serviceManagerMockedStatic;

    private static ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private static RequestDataCountTask requestDataCountTask;

    private static RemovalEventService removalEventService = new RemovalEventServiceImpl();

    @BeforeClass
    public static void setUp() {
        RemovalConfig removalConfig = new RemovalConfig();
        removalConfig.setEnableRemoval(true);
        removalConfig.setWindowsTime(1000);
        removalConfig.setWindowsNum(10);
        removalConfig.setExpireTime(6000);
        configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> ConfigManager.getConfig(RemovalConfig.class)).thenReturn(removalConfig);
        serviceManagerMockedStatic = Mockito.mockStatic(ServiceManager.class);
        serviceManagerMockedStatic.when(() -> ServiceManager.getService(RemovalEventService.class))
                .thenReturn(removalEventService);
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> {
            InstanceInfo info = InstanceCache.INSTANCE_MAP.computeIfAbsent(KEY, s -> new InstanceInfo());
            info.getRequestNum().addAndGet(REQ_NUM);
            info.getRequestFailNum().addAndGet(REQ_FAIL_NUM);
            info.setLastInvokeTime(System.currentTimeMillis());
        }, removalConfig.getWindowsTime(), removalConfig.getWindowsTime(), TimeUnit.MILLISECONDS);
    }

    @Test
    public void testProcessData() throws InterruptedException {
        requestDataCountTask = new RequestDataCountTask();
        requestDataCountTask.start();
        Thread.sleep(15000);
        InstanceInfo info = InstanceCache.INSTANCE_MAP.get(KEY);
        Assert.assertTrue(info != null && info.getCountDataList().size() > 0);
        info.getCountDataList().forEach(requestCountData -> {
            Assert.assertEquals(REQ_NUM, requestCountData.getRequestNum());
            Assert.assertEquals(REQ_FAIL_NUM, requestCountData.getRequestFailNum());
        });
    }

    @AfterClass
    public static void setDown() {
        if (configManagerMockedStatic != null) {
            configManagerMockedStatic.close();
        }
        InstanceCache.INSTANCE_MAP.clear();
        if (scheduledThreadPoolExecutor != null) {
            scheduledThreadPoolExecutor.shutdown();
        }
        if (requestDataCountTask != null) {
            requestDataCountTask.stop();
        }
        if (serviceManagerMockedStatic != null) {
            serviceManagerMockedStatic.close();
        }
    }
}