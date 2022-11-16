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

package com.huaweicloud.intergration.graceful;

import org.junit.Rule;
import org.junit.rules.TestRule;

/**
 * feign 优雅上下线测试测试
 *
 * @author zhouss
 * @since 2022-11-15
 */
public class FeignGracefulTest extends GracefulTest {
    @Rule(order = 301)
    public final TestRule rule = new GracefulRule();

    @Override
    protected String getBaseUrl() {
        return "http://localhost:8015";
    }
}
