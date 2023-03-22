/*
 * Copyright (C) 2022-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.intergration.router;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 基于静态配置文件配置标签的路由测试(非下发动态配置规则)
 *
 * @author provenceee
 * @since 2022-11-02
 */
public class RouterTest {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final int FEIGN_PORT = 8017;

    private static final int REST_PORT = 8022;

    private static final String IP = "http://127.0.0.1:";

    private static final String BOOT_BASE_PATH = "/router/boot/getMetadata?exit=";

    private static final String CLOUD_BASE_PATH = "/router/cloud/getMetadata?exit=";

    private static final String FEIGN_BOOT_BASE_PATH = IP + FEIGN_PORT + BOOT_BASE_PATH;

    private static final String FEIGN_CLOUD_BASE_PATH = IP + FEIGN_PORT + CLOUD_BASE_PATH;

    private static final String REST_CLOUD_BASE_PATH = IP + REST_PORT + CLOUD_BASE_PATH;

    private static final int TIMES = 30;

    private static final int WAIT_SECONDS = 300;

    @Rule
    public final RouterRule routerRule = new RouterRule();

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
    public void testRouterByFeign() throws InterruptedException {
        HttpHeaders headers = new HttpHeaders();

        // 测试命中Test-Env:env-001的实例
        headers.add("Test-Env", "env-001");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(FEIGN_BOOT_BASE_PATH + false, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Test-Env:env-001"));

            exchange = REST_TEMPLATE.exchange(FEIGN_CLOUD_BASE_PATH + false, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Test-Env:env-001"));
        }

        // 测试没有命中Test-Env:env-005的实例时，切换至无Test-Env的实例
        headers.clear();
        headers.add("Test-Env", "env-005");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(FEIGN_BOOT_BASE_PATH + false, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("Test-Env:"));

            exchange = REST_TEMPLATE.exchange(FEIGN_CLOUD_BASE_PATH + false, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("Test-Env:"));
        }

        // 测试没有路由请求头时，优先切换至无路由标签的实例
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(FEIGN_BOOT_BASE_PATH + false, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("Test-Env"));

            exchange = REST_TEMPLATE.exchange(FEIGN_CLOUD_BASE_PATH + false, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("Test-Env"));
        }

        // 停掉无路由标签的实例
        Assert.assertThrows(Exception.class, () -> REST_TEMPLATE
                .exchange(FEIGN_BOOT_BASE_PATH + true, HttpMethod.GET, new HttpEntity<>(null, new HttpHeaders()),
                        String.class));

        // 等待无路由标签的实例下线
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < WAIT_SECONDS; i++) {
            try {
                exchange = REST_TEMPLATE.exchange(FEIGN_BOOT_BASE_PATH + false, HttpMethod.GET, entity, String.class);
                if (Objects.requireNonNull(exchange.getBody()).contains("Test-Env")) {
                    exchange = REST_TEMPLATE
                            .exchange(FEIGN_CLOUD_BASE_PATH + false, HttpMethod.GET, entity, String.class);
                    if (Objects.requireNonNull(exchange.getBody()).contains("Test-Env")) {
                        // 下游实例已下线
                        break;
                    }
                }
            } catch (Exception ignored) {
                // 下游实例还未剔除，忽略
            }
            TimeUnit.SECONDS.sleep(1);
        }

        // 停掉标签为Test-Env1=env-002的实例
        headers.clear();
        headers.add("Test-Env1", "env-002");
        Assert.assertThrows(Exception.class, () -> REST_TEMPLATE
                .exchange(FEIGN_BOOT_BASE_PATH + true, HttpMethod.GET, new HttpEntity<>(null, headers),
                        String.class));

        // 等待标签为Test-Env1=env-002的实例下线
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < WAIT_SECONDS; i++) {
            try {
                exchange = REST_TEMPLATE.exchange(FEIGN_BOOT_BASE_PATH + false, HttpMethod.GET, entity, String.class);
                String body = Objects.requireNonNull(exchange.getBody());
                if (!body.contains("Test-Env1")) {
                    exchange = REST_TEMPLATE
                            .exchange(FEIGN_CLOUD_BASE_PATH + false, HttpMethod.GET, entity, String.class);
                    body = Objects.requireNonNull(exchange.getBody());
                    if (!body.contains("Test-Env1")) {
                        // 下游实例已下线
                        break;
                    }
                }
            } catch (Exception ignored) {
                // 下游实例还未剔除，忽略
            }
            TimeUnit.SECONDS.sleep(1);
        }

        // 测试没有命中Test-Env:env-005的实例时，切换至无Test-Env的实例，如果没有无Test-Env的实例，则返回空列表，即调用报错
        headers.clear();
        headers.add("Test-Env", "env-005");
        Assert.assertThrows(Exception.class, () -> REST_TEMPLATE.exchange(FEIGN_BOOT_BASE_PATH + false, HttpMethod.GET,
                new HttpEntity<>(null, new HttpHeaders(headers)), String.class));
    }

    /**
     * 测试根据请求信息路由，该测试用例会模拟停服务的情况，所以执行结果与用例顺序强相关
     */
    @Test
    public void testRouterByRest() throws InterruptedException {
        HttpHeaders headers = new HttpHeaders();

        // 测试命中Test-Env:env-001的实例
        headers.add("Test-Env", "env-001");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(REST_CLOUD_BASE_PATH + false, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("Test-Env:env-001"));
        }

        // 测试没有命中Test-Env:env-005的实例时，切换至无Test-Env的实例
        headers.clear();
        headers.add("Test-Env", "env-005");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(REST_CLOUD_BASE_PATH + false, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("Test-Env:"));
        }

        // 测试没有路由请求头时，优先切换至无路由标签的实例
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(REST_CLOUD_BASE_PATH + false, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("Test-Env"));
        }

        // 停掉无路由标签的实例
        Assert.assertThrows(Exception.class, () -> REST_TEMPLATE
                .exchange(REST_CLOUD_BASE_PATH + true, HttpMethod.GET, new HttpEntity<>(null, new HttpHeaders()),
                        String.class));

        // 等待无路由标签的实例下线
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < WAIT_SECONDS; i++) {
            try {
                exchange = REST_TEMPLATE
                        .exchange(REST_CLOUD_BASE_PATH + false, HttpMethod.GET, entity, String.class);
                if (Objects.requireNonNull(exchange.getBody()).contains("Test-Env")) {
                    // 下游实例已下线
                    break;
                }
            } catch (Exception ignored) {
                // 下游实例还未剔除，忽略
            }
            TimeUnit.SECONDS.sleep(1);
        }

        // 停掉标签为Test-Env1=env-002的实例
        headers.clear();
        headers.add("Test-Env1", "env-002");
        Assert.assertThrows(Exception.class, () -> REST_TEMPLATE
                .exchange(REST_CLOUD_BASE_PATH + true, HttpMethod.GET, new HttpEntity<>(null, headers),
                        String.class));

        // 等待标签为Test-Env1=env-002的实例下线
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < WAIT_SECONDS; i++) {
            try {
                exchange = REST_TEMPLATE.exchange(REST_CLOUD_BASE_PATH + false, HttpMethod.GET, entity, String.class);
                String body = Objects.requireNonNull(exchange.getBody());
                if (!body.contains("Test-Env1")) {
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
        Assert.assertThrows(Exception.class, () -> REST_TEMPLATE.exchange(REST_CLOUD_BASE_PATH + false, HttpMethod.GET,
                new HttpEntity<>(null, new HttpHeaders(headers)), String.class));
    }
}