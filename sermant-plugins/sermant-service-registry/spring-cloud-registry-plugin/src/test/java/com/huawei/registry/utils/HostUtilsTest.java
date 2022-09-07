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

package com.huawei.registry.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * 工具类测试
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class HostUtilsTest {
    @Test
    public void test() {
        String localIp = "127.0.0.1";
        Assert.assertFalse(HostUtils.isSameInstance(localIp, 8001, localIp, 8002));
        Assert.assertTrue(HostUtils.isSameInstance(localIp, 8001, localIp, 8001));
        Assert.assertFalse(HostUtils.isSameInstance(localIp + "1", 8001, localIp, 8001));
        Assert.assertTrue(HostUtils.isSameInstance(HostUtils.getHostName(), 8001, HostUtils.getMachineIp(), 8001));
        Assert.assertTrue(HostUtils.isSameInstance(HostUtils.getHostName(), 8001, HostUtils.getHostName(), 8001));
        Assert.assertTrue(HostUtils.isSameMachine(HostUtils.getHostName(), HostUtils.getHostName()));
        Assert.assertTrue(HostUtils.isSameMachine(HostUtils.getHostName(), HostUtils.getMachineIp()));
    }
}
