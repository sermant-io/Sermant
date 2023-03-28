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

package com.huaweicloud.intergration.registry.boot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpMethod;

/**
 * RestTemplate测试
 *
 * @author zhouss
 * @since 2022-10-26
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "BOOT_REGISTRY")
public class RestBootRegistryTest extends BootRegistryTest {
    /**
     * 测试rest
     */
    @Test
    public void testRest() {
        check("restRegistryPost", HttpMethod.GET);
        check("restRegistry", HttpMethod.GET);
    }

    @Override
    protected String getUrl() {
        return "http://localhost:8005/bootRegistry";
    }
}
