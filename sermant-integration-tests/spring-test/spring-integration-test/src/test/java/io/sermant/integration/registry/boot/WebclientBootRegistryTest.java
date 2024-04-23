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

package io.sermant.integration.registry.boot;

import io.sermant.integration.common.utils.EnvUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * webclient测试
 *
 * @author provenceee
 * @since 2023-05-23
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "BOOT_REGISTRY")
public class WebclientBootRegistryTest extends BootRegistryTest {
    /**
     * 此处将模拟请求两次, 基于轮询负载均衡将拿到两个不同结果
     */
    @Test
    public void testWebclient() {
        if (!shouldTest()) {
            return;
        }
        check("webClientRegistry?type=reactor", HttpMethod.GET);
        check("webClientRegistryPost?type=reactor", HttpMethod.GET);

        check("webClientRegistry?type=jetty", HttpMethod.GET);
        check("webClientRegistryPost?type=jetty", HttpMethod.GET);

        check("webClientRegistry?type=httpClient", HttpMethod.GET);
        check("webClientRegistryPost?type=httpClient", HttpMethod.GET);
    }

    @Override
    protected String getUrl() {
        return "http://localhost:8015/bootRegistry";
    }

    @Override
    protected Map<String, String> getLabels() {
        final Map<String, String> labels = new HashMap<>();
        labels.put("app", getType());
        labels.put("environment", "development");
        return labels;
    }

    @Override
    protected String getType() {
        return "feign";
    }

    // 2.1.0.RELEASE+才支持
    private boolean shouldTest() {
        final String env = EnvUtils.getEnv("spring.boot.version", null);
        if (env == null) {
            return true;
        }
        final String[] parts = env.split("\\.");
        if (parts.length != 4) {
            return true;
        }
        int majorVersion = Integer.parseInt(parts[0]);
        if (majorVersion < 2) {
            return false;
        }
        int minVersion = Integer.parseInt(parts[1]);
        return minVersion > 0;
    }
}
