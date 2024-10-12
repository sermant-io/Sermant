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

package io.sermant.removal.cache;

import io.sermant.removal.entity.InstanceInfo;
import io.sermant.removal.entity.RequestInfo;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class of service instance cache
 *
 * @author zhp
 * @since 2023-02-17
 */
public class InstanceCacheTest {
    private static final String HOST = "127.0.0.1";

    private static final String PORT = "8080";

    private static final String KEY = "127.0.0.1:8080";

    private static final int NUM = 10;

    private static final float RATE = 0.5f;

    @Test
    public void saveInstanceInfo() {
        long time = System.currentTimeMillis();
        for (int i = 0; i < NUM; i++) {
            RequestInfo requestInfo = new RequestInfo();
            requestInfo.setHost(HOST);
            requestInfo.setPort(PORT);
            requestInfo.setSuccess(true);
            requestInfo.setRequestTime(time);
            InstanceCache.saveInstanceInfo(requestInfo);
        }
        for (int i = 0; i < NUM; i++) {
            RequestInfo requestInfo = new RequestInfo();
            requestInfo.setHost(HOST);
            requestInfo.setPort(PORT);
            requestInfo.setSuccess(false);
            requestInfo.setRequestTime(time);
            InstanceCache.saveInstanceInfo(requestInfo);
        }
        Assert.assertTrue(InstanceCache.INSTANCE_MAP.containsKey(KEY));
        InstanceInfo instanceInfo = InstanceCache.INSTANCE_MAP.get(KEY);
        Assert.assertEquals(instanceInfo.getHost(), HOST);
        Assert.assertEquals(instanceInfo.getPort(), PORT);
        Assert.assertEquals(instanceInfo.getRequestNum().get(), NUM * 2);
        Assert.assertEquals(instanceInfo.getLastInvokeTime(), time);
    }
}
