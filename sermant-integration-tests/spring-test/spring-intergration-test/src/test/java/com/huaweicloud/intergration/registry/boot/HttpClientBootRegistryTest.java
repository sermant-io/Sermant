/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

import com.huaweicloud.intergration.common.utils.EnvUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpMethod;

/**
 * HttpClient相关测试
 *
 * @author zhouss
 * @since 2022-10-26
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "BOOT_REGISTRY")
public class HttpClientBootRegistryTest extends BootRegistryTest {
    /**
     * 默认Client
     */
    @Test
    public void testDefaultClient() {
        if (!canTestSync()) {
            return;
        }
        check("defaultHttpClientGet", HttpMethod.GET);
        check("defaultHttpClientPost", HttpMethod.GET);
    }

    /**
     * 测试minHttpClient
     */
    @Test
    public void testMinHttpClient() {
        if (!canTestSync()) {
            return;
        }
        check("minHttpClientGet", HttpMethod.GET);
        check("minHttpClientPost", HttpMethod.GET);
    }

    /**
     * 测试HttpClient
     */
    @Test
    public void testHttpClient() {
        if (!canTestSync()) {
            return;
        }
        check("httpClientGet", HttpMethod.GET);
        check("httpClientPost", HttpMethod.GET);
    }

    /**
     * 测试异步场景
     */
    @Test
    public void testAsync() {
        if (!canTestAsync()) {
            return;
        }
        check("httpAsyncClientGet", HttpMethod.GET);
        check("minimalHttpAsyncClientPost", HttpMethod.GET);
        check("minimalHttpAsyncClientThreadGet", HttpMethod.GET);
        check("httpAsyncClientThreadPost", HttpMethod.GET);
    }

    /**
     * 测试超时重试
     */
    @Test
    public void testRetry() {
        if (!canTestSync()) {
            return;
        }
        final String result = req("httpClientRetry", HttpMethod.GET);
        Assertions.assertNotNull(result);
    }

    private boolean canTestSync() {
        return !"1.5.x".equals(EnvUtils.getEnv("app.version", null));
    }

    private boolean canTestAsync() {
        // 当前仅支持httpclient版本为4.5.13, 关联httpAsyncClient为4.1.4
        final String env = EnvUtils.getEnv("http.client.version", null);
        return "4.5.13".equals(env);
    }
}
