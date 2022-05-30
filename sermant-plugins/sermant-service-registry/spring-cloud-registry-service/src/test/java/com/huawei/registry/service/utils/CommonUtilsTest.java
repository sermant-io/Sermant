/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.registry.service.utils;

import com.huawei.registry.config.RegisterConfig;
import com.huawei.registry.utils.CommonUtils;

import org.junit.Assert;
import org.junit.Test;

/**
 * 工具类测试
 *
 * @author zhouss
 * @since 2022-01-05
 */
public class CommonUtilsTest {

    @Test
    public void testGetPortByEndpoint() {
        final int portByEndpoint = CommonUtils.getPortByEndpoint("rest://localhost:8080");
        Assert.assertEquals(8080, portByEndpoint);
    }

    @Test
    public void testGetIpByEndpoint() {
        final String ipByEndpoint = CommonUtils.getIpByEndpoint("rest://127.0.0.1:8080").get();
        Assert.assertEquals("127.0.0.1", ipByEndpoint);
    }
}
