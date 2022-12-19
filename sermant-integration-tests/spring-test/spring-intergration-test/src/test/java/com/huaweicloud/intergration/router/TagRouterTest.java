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

import com.huaweicloud.intergration.config.supprt.KieClient;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
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
 * testTagRouterByZuu()和testTagRouterByGateway()执行是否成功依赖下发的配置，请勿更改两者代码顺序
 *
 * @author provenceee
 * @since 2022-11-14
 */
@FixMethodOrder(MethodSorters.JVM)
public class TagRouterTest {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final KieClient KIE_CLIENT = new KieClient(REST_TEMPLATE);

    private static final int ZUUL_PORT = 8000;

    private static final int GATEWAY_PORT = 8001;

    private static final String REST_KEY = "servicecomb.routeRule.rest-provider";

    private static final String FEIGN_KEY = "servicecomb.routeRule.feign-provider";

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
    public void testTagRouterByGateway() throws InterruptedException {
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

        // 测试标签匹配策略、大小写匹配、路由权重等功能
        configMatchTest(headers);
    }

    /**
     * 测试不等于匹配
     */
    private void noEquConfigTest(HttpHeaders headers) throws InterruptedException {
        headers.clear();
        String CONTENT = "---\n"
                + "- precedence: 1\n"
                + "  match:\n"
                + "    headers:\n"
                + "        id:\n"
                + "          noEqu: '1'\n"
                + "          caseInsensitive: false\n"
                + "  route:\n"
                + "    - tags:\n"
                + "        group: gray\n"
                + "      weight: 100\n";

        Assert.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assert.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        Thread.sleep(3000);
        // 不等于匹配测试，命中group:gray的实例
        headers.add("id", "2");
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
    }

    /**
     * 测试大于等于匹配
     */
    private void noLessConfigTest(HttpHeaders headers) throws InterruptedException {
        headers.clear();
        String CONTENT = "---\n"
                + "- precedence: 1\n"
                + "  match:\n"
                + "    headers:\n"
                + "        id:\n"
                + "          noLess: '1'\n"
                + "          caseInsensitive: false\n"
                + "  route:\n"
                + "    - tags:\n"
                + "        group: gray\n"
                + "      weight: 100\n";

        Assert.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assert.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        Thread.sleep(3000);
        // 大于等于匹配测试，命中group:gray的实例
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
    }

    /**
     * 测试小于等于匹配
     */
    private void noGreaterConfigTest(HttpHeaders headers) throws InterruptedException {
        headers.clear();
        String CONTENT = "---\n"
                + "- precedence: 1\n"
                + "  match:\n"
                + "    headers:\n"
                + "        id:\n"
                + "          noGreater: '1'\n"
                + "          caseInsensitive: false\n"
                + "  route:\n"
                + "    - tags:\n"
                + "        group: gray\n"
                + "      weight: 100\n";

        Assert.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assert.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        Thread.sleep(3000);
        // 小于等于匹配测试，命中group:gray的实例
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
    }

    /**
     * 测试大于匹配
     */
    private void greaterConfigTest(HttpHeaders headers) throws InterruptedException {
        headers.clear();
        String CONTENT = "---\n"
                + "- precedence: 1\n"
                + "  match:\n"
                + "    headers:\n"
                + "        id:\n"
                + "          greater: '1'\n"
                + "          caseInsensitive: false\n"
                + "  route:\n"
                + "    - tags:\n"
                + "        group: gray\n"
                + "      weight: 100\n";

        Assert.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assert.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        Thread.sleep(3000);
        // 大于匹配测试，命中group:gray的实例
        headers.add("id", "2");
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
    }

    /**
     * 测试小于匹配
     */
    private void lessConfigTest(HttpHeaders headers) throws InterruptedException {
        headers.clear();
        String CONTENT = "---\n"
                + "- precedence: 1\n"
                + "  match:\n"
                + "    headers:\n"
                + "        id:\n"
                + "          less: '1'\n"
                + "          caseInsensitive: false\n"
                + "  route:\n"
                + "    - tags:\n"
                + "        group: gray\n"
                + "      weight: 100\n";

        Assert.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assert.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        Thread.sleep(3000);
        // 小于匹配测试，命中group:gray的实例
        headers.add("id", "0");
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
    }

    /**
     * 测试正则表达式匹配
     */
    private void regexConfigTest(HttpHeaders headers) throws InterruptedException {
        headers.clear();
        String CONTENT = "---\n"
                + "- precedence: 1\n"
                + "  match:\n"
                + "    headers:\n"
                + "        id:\n"
                + "          regex: '^[0-9]*$'\n"
                + "          caseInsensitive: false\n"
                + "  route:\n"
                + "    - tags:\n"
                + "        group: gray\n"
                + "      weight: 100\n";

        Assert.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assert.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        Thread.sleep(3000);
        // 正则匹配测试，命中group:gray的实例
        headers.add("id", "5");
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
    }

    /**
     * 测试大小写敏感匹配
     */
    private void caseInsensitiveConfigTest(HttpHeaders headers) throws InterruptedException {
        headers.clear();
        String CONTENT = "---\n"
                + "- precedence: 1\n"
                + "  match:\n"
                + "    headers:\n"
                + "        name:\n"
                + "          exact: 'abc'\n"
                + "          caseInsensitive: true\n"
                + "  route:\n"
                + "    - tags:\n"
                + "        group: gray\n"
                + "      weight: 100\n";

        Assert.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assert.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        Thread.sleep(3000);
        // 大小写敏感测试，未命中group:gray的实例
        headers.add("name", "ABC");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(GATEWAY_REST_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_BOOT_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试权重匹配
     */
    private void weightConfigTest(HttpHeaders headers) throws InterruptedException {
        headers.clear();
        String CONTENT = "---\n"
                + "- precedence: 1\n"
                + "  match:\n"
                + "    headers:\n"
                + "        id:\n"
                + "          exact: '1'\n"
                + "          caseInsensitive: false\n"
                + "  route:\n"
                + "    - tags:\n"
                + "        group: gray\n"
                + "      weight: 0\n";

        Assert.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assert.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        Thread.sleep(3000);
        // 0权重路由测试，未命中group:gray的实例
        headers.add("id", "1");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(GATEWAY_REST_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_BOOT_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(GATEWAY_FEIGN_CLOUD_BASE_PATH, HttpMethod.GET, entity, String.class);
            Assert.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试标签匹配策略、大小写匹配、路由权重等功能
     */
    private void configMatchTest(HttpHeaders headers) throws InterruptedException {
        noEquConfigTest(headers);
        noGreaterConfigTest(headers);
        noLessConfigTest(headers);
        greaterConfigTest(headers);
        lessConfigTest(headers);
        regexConfigTest(headers);
        caseInsensitiveConfigTest(headers);
        weightConfigTest(headers);
    }
}