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

package com.huaweicloud.intergration.router;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 标签路由测试
 *
 * @author provenceee
 * @since 2022-11-14
 */
public class TagRouterTest {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final int ZUUL_PORT = 8000;

    private static final int GATEWAY_PORT = 8001;

    private static final String IP = "http://127.0.0.1:";

    private static final String BOOT_BASE_PATH = "/router/boot/getMetadata?exit=false";

    private static final String CLOUD_BASE_PATH = "/router/cloud/getMetadata?exit=false";

    private static final String REST_BASE_PATH = "/rest";

    private static final String FEIGN_BASE_PATH = "/feign";

    private static final String ZUUL_REST_CLOUD_BASE_PATH = IP + ZUUL_PORT + REST_BASE_PATH + CLOUD_BASE_PATH;

    private static final String ZUUL_FEIGN_BOOT_BASE_PATH = IP + ZUUL_PORT + FEIGN_BASE_PATH + BOOT_BASE_PATH;

    private static final String ZUUL_FEIGN_CLOUD_BASE_PATH = IP + ZUUL_PORT + FEIGN_BASE_PATH + CLOUD_BASE_PATH;

    private static final String GATEWAY_REST_CLOUD_BASE_PATH = IP + GATEWAY_PORT + REST_BASE_PATH + CLOUD_BASE_PATH;

    private static final String GATEWAY_FEIGN_BOOT_BASE_PATH = IP + GATEWAY_PORT + FEIGN_BASE_PATH + BOOT_BASE_PATH;

    private static final String GATEWAY_FEIGN_CLOUD_BASE_PATH = IP + GATEWAY_PORT + FEIGN_BASE_PATH + CLOUD_BASE_PATH;

    private static final int TIMES = 30;

    @Rule
    public final TagRouterRule routerRule = new TagRouterRule();

    public static final List<String> SPRING_CLOUD_VERSIONS_FOR_ZUUL = Arrays
        .asList("Edgware.SR2", "Finchley.RELEASE", "Greenwich.RELEASE", "Hoxton.RELEASE");

    public static final List<String> SPRING_CLOUD_VERSIONS_FOR_GATEWAY = Arrays
        .asList("Finchley.RELEASE", "Greenwich.RELEASE", "Hoxton.RELEASE", "2020.0.0", "2021.0.0", "2021.0.3");

    private final String springCloudVersion;

    /**
     * 构造方法
     */
    public TagRouterTest() {
        springCloudVersion = Optional.ofNullable(System.getenv("SPRING_CLOUD_VERSION")).orElse("Hoxton.RELEASE");
    }

    /**
     * 测试标签路由
     */
    @Test
    public void testTagRouterByZuul() {
        // SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (!SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            return;
        }
        HttpHeaders headers = new HttpHeaders();

        // 测试命中group:gray的实例
        headers.add("id", "1");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(ZUUL_REST_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(ZUUL_FEIGN_BOOT_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(ZUUL_FEIGN_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }

        // 测试命中version:1.0.1的实例
        headers.clear();
        headers.add("name", "BAr");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(ZUUL_REST_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.1"));

            exchange = REST_TEMPLATE.exchange(ZUUL_FEIGN_BOOT_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.1"));

            exchange = REST_TEMPLATE.exchange(ZUUL_FEIGN_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.1"));
        }

        // 测试没有命中version:1.0.1的实例
        headers.clear();
        headers.add("name", "foo");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(ZUUL_REST_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            String body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(ZUUL_FEIGN_BOOT_BASE_PATH, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(ZUUL_FEIGN_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));
        }

        // 测试没有命中version:1.0.1的实例
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(ZUUL_REST_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            String body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(ZUUL_FEIGN_BOOT_BASE_PATH, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(ZUUL_FEIGN_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));
        }
    }

    /**
     * 测试标签路由
     */
    @Test
    public void testTagRouterByGateway() {
        // SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (!SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            return;
        }
        HttpHeaders headers = new HttpHeaders();

        // 测试命中group:gray的实例
        headers.add("id", "1");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(GATEWAY_REST_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_BOOT_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }

        // 测试命中version:1.0.1的实例
        headers.clear();
        headers.add("name", "BAr");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(GATEWAY_REST_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.1"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_BOOT_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.1"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.1"));
        }

        // 测试没有命中version:1.0.1的实例
        headers.clear();
        headers.add("name", "foo");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(GATEWAY_REST_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            String body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_BOOT_BASE_PATH, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));
        }

        // 测试没有命中version:1.0.1的实例
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(GATEWAY_REST_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            String body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_BOOT_BASE_PATH, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assert.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));
        }
    }
}