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

package io.sermant.integration.registry.boot;

import io.sermant.integration.common.utils.EnvUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpMethod;

/**
 * HttpURLConnection测试
 *
 * @author zhouss
 * @since 2022-10-26
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "BOOT_REGISTRY")
public class UrlConnectionBootRegistryTest extends BootRegistryTest {
    @Test
    public void testUrlConnection() {
        if (!canTestSync()) {
            return;
        }
        check("urlConnectionGet", HttpMethod.GET);
        check("urlConnectionPostNoEntity", HttpMethod.GET);
    }

    @Test
    public void testRetry() {
        if (!canTestSync()) {
            return;
        }
        final String urlConnectionRetry = req("urlConnectionRetry", HttpMethod.GET);
        Assertions.assertNotNull(urlConnectionRetry);
    }

    private boolean canTestSync() {
        return !"1.5.x".equals(EnvUtils.getEnv("app.version", null));
    }

    @Override
    protected String getUrl() {
        return "http://localhost:8005/bootRegistry";
    }
}
