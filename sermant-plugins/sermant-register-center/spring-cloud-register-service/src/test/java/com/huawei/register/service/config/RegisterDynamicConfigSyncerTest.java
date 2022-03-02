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

package com.huawei.register.service.config;

import com.huawei.register.config.RegisterDynamicConfigSyncer;
import com.huawei.register.service.client.BaseTest;

import org.junit.Test;

/**
 * 测试配置添加
 *
 * @author zhouss
 * @since 2022-01-05
 */
public class RegisterDynamicConfigSyncerTest extends BaseTest {

    @Test
    public void testAddConfigListener() {
        new RegisterDynamicConfigSyncer().start();
    }
}
