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

package com.huaweicloud.integration.router;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 区域路由测试
 *
 * @author provenceee
 * @since 2022-09-28
 */
@EnabledIfEnvironmentVariable(named = "TEST_TYPE", matches = "router")
public class RouterTest {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final String BASE_URL = "http://127.0.0.1:28020/consumer/";

    private static final int TIMES = 30;

    private static final int WAIT_SECONDS = 30;

    private final String zone;

    /**
     * 构造方法
     */
    public RouterTest() {
        zone = Optional.ofNullable(System.getenv("SERVICE_META_ZONE")).orElse("bar");
    }

    /**
     * 测试根据请求信息路由，该测试用例会模拟停服务的情况，所以执行结果与用例顺序强相关
     */
    @Test
    public void testRouter() throws InterruptedException {
        HttpHeaders headers = new HttpHeaders();

        // 测试命中Test-Env:env-001的实例
        headers.add("Test-Env", "env-001");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(BASE_URL + "getZone?exit=false", HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Test-Env:env-001"));
        }

        // 测试没有命中Test-Env:env-005的实例时，切换至无Test-Env的实例
        headers.clear();
        headers.add("Test-Env", "env-005");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(BASE_URL + "getZone?exit=false", HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("Test-Env:"));
        }

        // 测试没有路由请求头时，优先切换至无路由标签的实例
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(BASE_URL + "getZone?exit=false", HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("Test-Env"));
        }

        // 停掉无路由标签的实例
        Assertions.assertThrows(Exception.class, () -> REST_TEMPLATE
            .exchange(BASE_URL + "getZone?exit=true", HttpMethod.GET, new HttpEntity<>(null, new HttpHeaders()),
                String.class));

        // 等待无路由标签的实例下线
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < WAIT_SECONDS; i++) {
            try {
                exchange = REST_TEMPLATE
                    .exchange(BASE_URL + "getZone?exit=false", HttpMethod.GET, entity, String.class);
                if (Objects.requireNonNull(exchange.getBody()).contains("Test-Env")) {
                    // 下游实例已下线
                    break;
                }
            } catch (Exception ignored) {
                // 下游实例还未剔除，忽略
            }
            TimeUnit.SECONDS.sleep(1);
        }

        // 测试没有路由请求头时，切换至无路由标签的实例，如果都有标签，则随机选取
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(BASE_URL + "getZone?exit=false", HttpMethod.GET, entity, String.class);
            String body = Objects.requireNonNull(exchange.getBody());
            // 优先选取同az实例
            Assertions.assertTrue(body.contains("Test-Env") && body.contains(zone));
        }

        // 停掉az为bar的带有标签实例
        Assertions.assertThrows(Exception.class, () -> REST_TEMPLATE
            .exchange(BASE_URL + "getZone?exit=true", HttpMethod.GET, new HttpEntity<>(null, new HttpHeaders()),
                String.class));

        // 等待az为bar的带有标签实例下线
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < WAIT_SECONDS; i++) {
            try {
                exchange = REST_TEMPLATE
                    .exchange(BASE_URL + "getZone?exit=false", HttpMethod.GET, entity, String.class);
                String body = Objects.requireNonNull(exchange.getBody());
                if (body.contains("Test-Env") && !body.contains(zone)) {
                    // 下游实例已下线
                    break;
                }
            } catch (Exception ignored) {
                // 下游实例还未剔除，忽略
            }
            TimeUnit.SECONDS.sleep(1);
        }

        // 测试没有命中Test-Env:env-005的实例时，切换至无Test-Env的实例，如果没有无Test-Env的实例，则返回空列表，即调用报错
        headers.clear();
        headers.add("Test-Env", "env-005");
        Assertions.assertThrows(Exception.class, () -> REST_TEMPLATE
            .exchange(BASE_URL + "getZone?exit=true", HttpMethod.GET, new HttpEntity<>(null, new HttpHeaders(headers)),
                String.class));

        // 测试同az实例下线时，切换至其它az实例
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(BASE_URL + "getZone?exit=false", HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains(zone));
        }
    }
}