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

import com.huaweicloud.intergration.config.supprt.KieClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 标签路由测试
 *
 * @author provenceee
 * @since 2022-11-14
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "TAG_ROUTER")
public class TagRouterTest {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private static final KieClient KIE_CLIENT = new KieClient(REST_TEMPLATE);

    private static final int ZUUL_PORT = 8000;

    private static final int GATEWAY_PORT = 8001;

    private static final String REST_KEY = "servicecomb.routeRule.rest-provider";

    private static final String FEIGN_KEY = "servicecomb.routeRule.feign-provider";

    private static final String GLOBAL_KEY = "servicecomb.globalRouteRule";

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

    private static final String SERVICE_CONFIG = "SERVICE_CONFIG";

    private static final String GLOBAL_CONFIG = "GLOBAL_CONFIG";

    public static final List<String> SPRING_CLOUD_VERSIONS_FOR_ZUUL = Arrays
            .asList("Edgware.SR2", "Finchley.RELEASE", "Greenwich.RELEASE", "Hoxton.RELEASE");

    public static final List<String> SPRING_CLOUD_VERSIONS_FOR_GATEWAY = Arrays
            .asList("Finchley.RELEASE", "Greenwich.RELEASE", "Hoxton.RELEASE", "2020.0.0", "2021.0.0", "2021.0.3");

    private final String springCloudVersion;

    /**
     * 构造方法
     */
    public TagRouterTest() throws InterruptedException {
        springCloudVersion = Optional.ofNullable(System.getenv("SPRING_CLOUD_VERSION")).orElse("Hoxton.RELEASE");
        clearConfig();
    }

    /**
     * 标签路由测试：只下发服务粒度的flow匹配规则
     */
    @Test
    public void testRouterWithFlowMatchRule() throws InterruptedException {
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testFlowMatchRule(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH, SERVICE_CONFIG);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testFlowMatchRule(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH, SERVICE_CONFIG);
        }
        clearConfig();
    }

    /**
     * 标签路由测试：只下发全局粒度的flow匹配规则
     */
    @Test
    public void testRouterWithGlobalFlowMatchRule() throws InterruptedException {
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testFlowMatchRule(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH, GLOBAL_CONFIG);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testFlowMatchRule(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH, GLOBAL_CONFIG);
        }
        clearConfig();
    }

    /**
     * 标签路由测试：同时下发服务粒度和全局粒度的flow匹配规则
     */
    @Test
    public void testRouterWithFlowMatchRuleAndGlobalRule() throws InterruptedException {
        // 先下发flow匹配的全局路由规则
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "          id:\n"
                + "            exact: '1'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: yellow\n"
                + "          weight: 100\n"
                + "    - precedence: 2\n"
                + "      match:\n"
                + "        headers:\n"
                + "          name:\n"
                + "            exact: 'bar'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            version: 0.0.0\n"
                + "          weight: 100";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(GLOBAL_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);

        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testFlowMatchRule(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH, SERVICE_CONFIG);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testFlowMatchRule(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH, SERVICE_CONFIG);
        }
        clearConfig();
    }

    /**
     * 标签路由测试：只下发服务粒度的tag匹配规则
     */
    @Test
    public void testRouterWithTagMatchRule() throws InterruptedException {
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testTagMatchRule(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH, SERVICE_CONFIG);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testTagMatchRule(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH, SERVICE_CONFIG);
        }
        clearConfig();
    }

    /**
     * 标签路由测试：只下发全局粒度的tag匹配规则
     */
    @Test
    public void testRouterWithGlobalTagMatchRule() throws InterruptedException {
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testTagMatchRule(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH, GLOBAL_CONFIG);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testTagMatchRule(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH, GLOBAL_CONFIG);
        }
        clearConfig();
    }

    /**
     * 标签路由测试：同时下发服务粒度和全局粒度的tag匹配规则
     */
    @Test
    public void testRouterWithTagMatchRuleAndGlobalRule() throws InterruptedException {
        // 先下发tag匹配的全局路由规则
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          group:\n"
                + "            exact: 'red'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n"
                + "    - precedence: 2\n"
                + "      match:\n"
                + "        tags:\n"
                + "          group:\n"
                + "            exact: 'gray'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            version: 1.0.1\n"
                + "          weight: 100";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(GLOBAL_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);

        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testTagMatchRule(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH, GLOBAL_CONFIG);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testTagMatchRule(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH, GLOBAL_CONFIG);
        }
        clearConfig();
    }

    /**
     * 标签路由测试：同时下发flow匹配规则和tag匹配规则
     */
    @Test
    public void testRouterWithFlowAndTagRules() throws InterruptedException {
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testFlowAndTagMatchRule(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testFlowAndTagMatchRule(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH);
        }
        clearConfig();
    }

    /**
     * 标签路由测试：同标签路由场景测试（只下发tag匹配规则）
     */
    @Test
    public void testRouterWithConsumerTagRule() throws InterruptedException {
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testConsumerTagRule(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testConsumerTagRule(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH);
        }
        clearConfig();
    }

    /**
     * 测试标签匹配策略、大小写匹配、路由权重等功能
     */
    @Test
    public void testConfigMatchType() throws InterruptedException {
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testConfigMatch(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testConfigMatch(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH);
        }
    }

    private void testConfigMatch(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        noEquConfigTest(restCloudBasePath, feignBootBasePath, feignCloudBasePath);
        noGreaterConfigTest(restCloudBasePath, feignBootBasePath, feignCloudBasePath);
        noLessConfigTest(restCloudBasePath, feignBootBasePath, feignCloudBasePath);
        greaterConfigTest(restCloudBasePath, feignBootBasePath, feignCloudBasePath);
        lessConfigTest(restCloudBasePath, feignBootBasePath, feignCloudBasePath);
        inConfigTest(restCloudBasePath, feignBootBasePath, feignCloudBasePath);
        regexConfigTest(restCloudBasePath, feignBootBasePath, feignCloudBasePath);
        caseInsensitiveConfigTest(restCloudBasePath, feignBootBasePath, feignCloudBasePath);
        weightConfigTest(restCloudBasePath, feignBootBasePath, feignCloudBasePath);
        clearConfig();
    }

    /**
     * 测试flow匹配规则的标签路由功能
     */
    private void testFlowMatchRule(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath, String type) throws InterruptedException {
        // 下发flow匹配的路由规则
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "          id:\n"
                + "            exact: '1'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n"
                + "    - precedence: 2\n"
                + "      match:\n"
                + "        headers:\n"
                + "          name:\n"
                + "            exact: 'bar'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            version: 1.0.1\n"
                + "          weight: 100";

        if (SERVICE_CONFIG.equals(type)) {
            Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
            Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        }
        if (GLOBAL_CONFIG.equals(type)) {
            Assertions.assertTrue(KIE_CLIENT.publishConfig(GLOBAL_KEY, CONTENT));
        }
        TimeUnit.SECONDS.sleep(3);

        HttpHeaders headers = new HttpHeaders();

        // 测试命中group:gray的实例
        headers.add("id", "1");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }

        // 测试命中version:1.0.1的实例
        headers.clear();
        headers.add("name", "BAr");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.1"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.1"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.1"));
        }

        // 测试没有命中version:1.0.1的实例
        headers.clear();
        headers.add("name", "foo");
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            String body = Objects.requireNonNull(exchange.getBody());
            Assertions.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assertions.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assertions.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));
        }

        // 测试没有命中version:1.0.1的实例
        headers.clear();
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            String body = Objects.requireNonNull(exchange.getBody());
            Assertions.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assertions.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assertions.assertTrue(!body.contains("1.0.1") && !body.contains("group:gray"));
        }
    }

    /**
     * 测试tag匹配规则的标签路由功能
     */
    private void testTagMatchRule(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath, String type) throws InterruptedException {
        // 下发tag匹配的路由规则
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          group:\n"
                + "            exact: 'red'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n"
                + "    - precedence: 2\n"
                + "      match:\n"
                + "        tags:\n"
                + "          group:\n"
                + "            exact: 'gray'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            version: 1.0.0\n"
                + "          weight: 100";

        if (SERVICE_CONFIG.equals(type)) {
            Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
            Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        }
        if (GLOBAL_CONFIG.equals(type)) {
            Assertions.assertTrue(KIE_CLIENT.publishConfig(GLOBAL_KEY, CONTENT));
        }
        TimeUnit.SECONDS.sleep(3);

        HttpHeaders headers = new HttpHeaders();

        // 测试命中version:1.0.0的实例(consumer自身group:gray)
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.0"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.0"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("1.0.0"));
        }

        // 下发tag匹配的路由规则
        CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          group:\n"
                + "            exact: 'gray'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n"
                + "    - precedence: 2\n"
                + "      match:\n"
                + "        tags:\n"
                + "          group:\n"
                + "            exact: 'red'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            version: 1.0.0\n"
                + "          weight: 100";

        if (SERVICE_CONFIG.equals(type)) {
            Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
            Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        }
        if (GLOBAL_CONFIG.equals(type)) {
            Assertions.assertTrue(KIE_CLIENT.publishConfig(GLOBAL_KEY, CONTENT));
        }
        TimeUnit.SECONDS.sleep(3);

        // 测试命中group:gray的实例(consumer自身group:gray)
        entity = new HttpEntity<>(null, headers);
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试同时存在flow和tag匹配规则的标签路由功能
     */
    private void testFlowAndTagMatchRule(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        // 下发flow和tag匹配的路由规则
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "          id:\n"
                + "            exact: '1'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: red\n"
                + "          weight: 100\n"
                + "    - precedence: 2\n"
                + "      match:\n"
                + "        headers:\n"
                + "          name:\n"
                + "            exact: 'bar'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            version: 1.0.1\n"
                + "          weight: 100\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          group:\n"
                + "            exact: 'gray'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n"
                + "    - precedence: 2\n"
                + "      match:\n"
                + "        tags:\n"
                + "          group:\n"
                + "            exact: 'red'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            version: 1.0.0\n"
                + "          weight: 100";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);

        // 测试命中标签为version:1.0.1和group:gray标签的实例
        HttpHeaders headers = new HttpHeaders();
        headers.add("name", "BAr");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            String body = Objects.requireNonNull(exchange.getBody());
            Assertions.assertTrue(body.contains("1.0.1") && body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assertions.assertTrue(body.contains("1.0.1") && body.contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            body = Objects.requireNonNull(exchange.getBody());
            Assertions.assertTrue(body.contains("1.0.1") && body.contains("group:gray"));
        }
    }

    /**
     * 测试tag匹配规则的标签路由功能
     */
    private void testConsumerTagRule(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        // 下发同标签路由的路由规则
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          group:\n"
                + "            exact: 'gray'\n"
                + "            caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: CONSUMER_TAG\n";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);

        HttpHeaders headers = new HttpHeaders();

        // 测试命中group:CONSUMER_TAG的实例(consumer自身group:gray)
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("gray"));
        }
    }

    /**
     * 测试不等于匹配
     */
    private void noEquConfigTest(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "            id:\n"
                + "              noEqu: '1'\n"
                + "              caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);
        // 不等于匹配测试，命中group:gray的实例
        HttpHeaders headers = new HttpHeaders();
        headers.add("id", "2");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试大于等于匹配
     */
    private void noLessConfigTest(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "            id:\n"
                + "              noLess: '1'\n"
                + "              caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);
        // 大于等于匹配测试，命中group:gray的实例
        HttpHeaders headers = new HttpHeaders();
        headers.add("id", "1");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试小于等于匹配
     */
    private void noGreaterConfigTest(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "            id:\n"
                + "              noGreater: '1'\n"
                + "              caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);
        // 小于等于匹配测试，命中group:gray的实例
        HttpHeaders headers = new HttpHeaders();
        headers.add("id", "1");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试大于匹配
     */
    private void greaterConfigTest(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "            id:\n"
                + "              greater: '1'\n"
                + "              caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);
        // 大于匹配测试，命中group:gray的实例
        HttpHeaders headers = new HttpHeaders();
        headers.add("id", "2");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试小于匹配
     */
    private void lessConfigTest(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "            id:\n"
                + "              less: '1'\n"
                + "              caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);
        // 小于匹配测试，命中group:gray的实例
        HttpHeaders headers = new HttpHeaders();
        headers.add("id", "0");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试包含匹配
     */
    private void inConfigTest(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "            id:\n"
                + "              in:\n"
                + "                - 1\n"
                + "                - 2\n"
                + "              caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);
        // 小于匹配测试，命中group:gray的实例
        HttpHeaders headers = new HttpHeaders();
        headers.add("id", "2");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试正则表达式匹配
     */
    private void regexConfigTest(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "            id:\n"
                + "              regex: '^[0-9]*$'\n"
                + "              caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);
        // 正则匹配测试，命中group:gray的实例
        HttpHeaders headers = new HttpHeaders();
        headers.add("id", "5");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试大小写敏感匹配
     */
    private void caseInsensitiveConfigTest(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "            name:\n"
                + "              exact: 'abc'\n"
                + "              caseInsensitive: true\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 100\n";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);
        // 大小写敏感测试，未命中group:gray的实例
        HttpHeaders headers = new HttpHeaders();
        headers.add("name", "ABC");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    /**
     * 测试权重匹配
     */
    private void weightConfigTest(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/flow\n"
                + "  description: flow-rule-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        headers:\n"
                + "            id:\n"
                + "              exact: '1'\n"
                + "              caseInsensitive: false\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            group: gray\n"
                + "          weight: 0\n";

        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);
        // 0权重路由测试，未命中group:gray的实例
        HttpHeaders headers = new HttpHeaders();
        headers.add("id", "1");
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertFalse(Objects.requireNonNull(exchange.getBody()).contains("group:gray"));
        }
    }

    private void clearConfig() throws InterruptedException {
        KIE_CLIENT.deleteKey(REST_KEY);
        KIE_CLIENT.deleteKey(FEIGN_KEY);
        KIE_CLIENT.deleteKey(GLOBAL_KEY);
        TimeUnit.SECONDS.sleep(3);
    }
}