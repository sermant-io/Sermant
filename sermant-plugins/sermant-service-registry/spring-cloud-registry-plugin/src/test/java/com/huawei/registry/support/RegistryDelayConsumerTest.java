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

package com.huawei.registry.support;

import com.huawei.registry.config.ConfigConstants;

import org.junit.Assert;
import org.junit.Test;

/**
 * 测试睡眠
 *
 * @author zhouss
 * @since 2022-09-07
 */
public class RegistryDelayConsumerTest {
    @Test
    public void accept() {
        final RegistryDelayConsumer registryDelayConsumer = new RegistryDelayConsumer();
        long sleepS = 1L;
        final long start = System.currentTimeMillis();
        registryDelayConsumer.accept(sleepS);
        Assert.assertTrue(System.currentTimeMillis() - start >= sleepS * ConfigConstants.SEC_DELTA );
    }
}
