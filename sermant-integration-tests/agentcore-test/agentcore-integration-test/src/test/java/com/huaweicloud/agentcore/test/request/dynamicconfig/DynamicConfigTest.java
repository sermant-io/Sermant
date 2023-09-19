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

package com.huaweicloud.agentcore.test.request.dynamicconfig;

import com.huaweicloud.agentcore.test.request.utils.RequestUtils;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * 动态配置测试方法，采用http请求调用方式测试
 *
 * @author tangle
 * @since 2023-09-07
 */
@EnabledIfSystemProperty(named = "agentcore.test.type", matches = "DYNAMIC_CONFIG")
public class DynamicConfigTest {
    @Test
    public void testDynamicConfig() throws IOException {
        RequestUtils.testRequest("http://127.0.0.1:8915/testDynamicConfig");
    }
}
