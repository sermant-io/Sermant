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

package com.huawei.discovery.service.lb.stats;

import com.huawei.discovery.service.lb.rule.BaseTest;

import org.junit.Assert;
import org.junit.Test;

/**
 * 实例状态统计测试
 *
 * @author zhouss
 * @since 2022-10-09
 */
public class InstanceStatsTest extends BaseTest {
    @Test
    public void beforeRequest() {
        final InstanceStats stats = new InstanceStats();
        stats.beforeRequest();
        Assert.assertEquals(stats.getActiveRequests(), 1);
        Assert.assertEquals(stats.getAllRequestCount().get(), 1);
    }

    @Test
    public void errorRequest() {
        final InstanceStats stats = new InstanceStats();
        long consumerMs = 100L;
        stats.errorRequest(new Exception("ss"), consumerMs);
        Assert.assertEquals(stats.getFailRequestCount().get(), 1);
        Assert.assertEquals(stats.getActiveRequests(), 0);
        Assert.assertEquals(stats.getAllRequestConsumeTime().get(), 100);
        stats.beforeRequest();
        stats.errorRequest(new Exception("ss"), consumerMs);
        Assert.assertEquals(stats.getActiveRequests(), 0);

    }

    @Test
    public void afterRequest() {
        final InstanceStats stats = new InstanceStats();
        long consumerMs = 100L;
        stats.beforeRequest();
        stats.afterRequest(consumerMs);
        Assert.assertEquals(stats.getActiveRequests(), 0);
    }

    @Test
    public void completeRequest() {
        final InstanceStats stats = new InstanceStats();
        stats.completeRequest();
        Assert.assertEquals(stats.getActiveRequests(), 0);
    }

    @Test
    public void testActiveRequest() throws InterruptedException {
        lbConfig.setActiveRequestTimeoutWindowMs(1L);
        final InstanceStats stats = new InstanceStats();
        stats.beforeRequest();
        Thread.sleep(10);
        Assert.assertEquals(0, stats.getActiveRequests());
    }
}
