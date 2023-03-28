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
 * 同AZ优先能力测试
 *
 * @author robotLJW
 * @since 2023-3-9
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "TAG_ROUTER_AZ")
public class TagRouterAzTest {
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

    public static final List<String> SPRING_CLOUD_VERSIONS_FOR_ZUUL = Arrays
            .asList("Edgware.SR2", "Finchley.RELEASE", "Greenwich.RELEASE", "Hoxton.RELEASE");

    public static final List<String> SPRING_CLOUD_VERSIONS_FOR_GATEWAY = Arrays
            .asList("Finchley.RELEASE", "Greenwich.RELEASE", "Hoxton.RELEASE", "2020.0.0", "2021.0.0", "2021.0.3");

    private final String springCloudVersion;

    public TagRouterAzTest() throws InterruptedException {
        springCloudVersion = Optional.ofNullable(System.getenv("SPRING_CLOUD_VERSION")).orElse("Hoxton.RELEASE");
        clearConfig();
    }

    /**
     * given: 标签路由测试：同标签路由场景测试（规则含有TriggerThreshold的policy）
     * when: 触发大于triggerThreshold
     * then: 执行同AZ优先策略
     */
    @Test
    public void testRouterWithTriggerThresholdPolicyRuleOne() throws InterruptedException {
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testTriggerThresholdPolicyAZRule(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testTriggerThresholdPolicyAZRule(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH);
        }
        clearConfig();
    }

    /**
     * given：标签路由测试：同标签路由场景测试（规则含有TriggerThreshold的policy）
     * when：触发小于triggerThreshold
     * then：非同AZ优先策略
     *
     * @throws InterruptedException
     */
    @Test
    public void testRouterWithTriggerThresholdPolicyRuleTwo() throws InterruptedException {
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testTriggerThresholdPolicyAZRuleTwo(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testTriggerThresholdPolicyAZRuleTwo(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH);
        }
        clearConfig();
    }

    /**
     * given：测试tag匹配规则同AZ优先标签路由功能：规则含有TriggerThreshold和minAllInstances的policy
     * when：大于minAllInstances和大于triggerThreshold场景
     * then：同AZ优先策略
     *
     * @throws InterruptedException
     */

    @Test
    public void testRouterPolicyRuleOne()throws InterruptedException{
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testPolicyAZRuleOne(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testPolicyAZRuleOne(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH);
        }
        clearConfig();
    }

    /**
     * given：测试tag匹配规则同AZ优先标签路由功能：规则含有TriggerThreshold和minAllInstances的policy
     * when：大于minAllInstances和小于triggerThreshold场景
     * then：非同AZ优先策略
     *
     * @throws InterruptedException
     */
    @Test
    public void testRouterPolicyRuleTwo()throws InterruptedException{
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testPolicyAZRuleTwo(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testPolicyAZRuleTwo(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH);
        }
        clearConfig();
    }

    /**
     * given：测试tag匹配规则同AZ优先标签路由功能：规则含有TriggerThreshold和minAllInstances的policy
     * when：小于minAllInstances和大于triggerThreshold场景
     * then：同AZ优先策略
     *
     * @throws InterruptedException
     */
    @Test
    public void testRouterPolicyRuleThree()throws InterruptedException{
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testPolicyAZRuleThree(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testPolicyAZRuleThree(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH);
        }
        clearConfig();
    }

    /**
     * given：测试tag匹配规则同AZ优先标签路由功能：规则含有TriggerThreshold和minAllInstances的policy
     * when：小于minAllInstances和小于triggerThreshold场景
     * then：同AZ优先策略
     *
     * @throws InterruptedException
     */
    @Test
    public void testRouterPolicyRuleFour()throws InterruptedException{
        // 测试zuul场景：SPRING_CLOUD_VERSIONS_FOR_ZUUL中的版本，才带有zuul的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_ZUUL.contains(springCloudVersion)) {
            testPolicyAZRuleFour(ZUUL_REST_CLOUD_BASE_PATH, ZUUL_FEIGN_BOOT_BASE_PATH, ZUUL_FEIGN_CLOUD_BASE_PATH);
        }

        // 测试gateway场景：SPRING_CLOUD_VERSIONS_FOR_GATEWAY中的版本，才带有gateway的依赖
        if (SPRING_CLOUD_VERSIONS_FOR_GATEWAY.contains(springCloudVersion)) {
            testPolicyAZRuleFour(GATEWAY_REST_CLOUD_BASE_PATH, GATEWAY_FEIGN_BOOT_BASE_PATH, GATEWAY_FEIGN_CLOUD_BASE_PATH);
        }
        clearConfig();
    }

    /**
     * 测试tag匹配规则同AZ优先标签路由功能：规则含有TriggerThreshold的policy
     * 触发大于triggerThreshold
     *
     * @param restCloudBasePath
     * @param feignBootBasePath
     * @param feignCloudBasePath
     * @throws InterruptedException
     */
    public void testTriggerThresholdPolicyAZRule(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        // 规则含有TriggerThreshold的policy
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-az-rule-trigger-threshold-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          zone:\n"
                + "            exact: 'az1'\n"
                + "            caseInsensitive: false\n"
                + "        policy:\n"
                + "          triggerThreshold: 40\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            zone: CONSUMER_TAG\n";
        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);

        HttpHeaders headers = new HttpHeaders();

        // 测试命中group:CONSUMER_TAG的实例(consumer自身zone:az1)
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));
        }
    }

    /**
     * 测试tag匹配规则同AZ优先标签路由功能：规则含有TriggerThreshold的policy
     * 触发小于于triggerThreshold
     *
     * @param restCloudBasePath
     * @param feignBootBasePath
     * @param feignCloudBasePath
     * @throws InterruptedException
     */
    public void testTriggerThresholdPolicyAZRuleTwo(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        // 规则含有TriggerThreshold的policy
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-az-rule-trigger-threshold-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          zone:\n"
                + "            exact: 'az1'\n"
                + "            caseInsensitive: false\n"
                + "        policy:\n"
                + "          triggerThreshold: 90\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            zone: CONSUMER_TAG\n";
        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);

        HttpHeaders headers = new HttpHeaders();

        // 测试命中group:CONSUMER_TAG的实例(consumer自身zone:az1)
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        int restCloudAZ1 = 0, restCloudAZ2 = 0;
        int feignBootAZ1 = 0, feignBootAZ2 = 0;
        int feignCloudAZ1 = 0, feignCloudAZ2 = 0;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            if (Objects.requireNonNull(exchange.getBody()).contains("az1")) {
                restCloudAZ1++;
            } else if (Objects.requireNonNull(exchange.getBody()).contains("az2")) {
                restCloudAZ2++;
            }

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            if (Objects.requireNonNull(exchange.getBody()).contains("az1")) {
                feignBootAZ1++;
            } else if (Objects.requireNonNull(exchange.getBody()).contains("az2")) {
                feignBootAZ2++;
            }

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            if (Objects.requireNonNull(exchange.getBody()).contains("az1")) {
                feignCloudAZ1++;
            } else if (Objects.requireNonNull(exchange.getBody()).contains("az2")) {
                feignCloudAZ2++;
            }
        }
        Assertions.assertNotEquals(0, restCloudAZ1);
        Assertions.assertNotEquals(0, restCloudAZ2);
        Assertions.assertNotEquals(0, feignBootAZ1);
        Assertions.assertNotEquals(0, feignBootAZ2);
        Assertions.assertNotEquals(0, feignCloudAZ1);
        Assertions.assertNotEquals(0, feignCloudAZ2);
    }

    /**
     * 测试tag匹配规则同AZ优先标签路由功能：规则含有TriggerThreshold和minAllInstances的policy
     * 大于minAllInstances和大于triggerThreshold场景
     * 同AZ优先策略
     *
     * @param restCloudBasePath
     * @param feignBootBasePath
     * @param feignCloudBasePath
     * @throws InterruptedException
     */
    public void testPolicyAZRuleOne(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        // 规则含有TriggerThreshold的policy
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-az-rule-trigger-threshold-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          zone:\n"
                + "            exact: 'az1'\n"
                + "            caseInsensitive: false\n"
                + "        policy:\n"
                + "          triggerThreshold: 40\n"
                + "          minAllInstances: 2\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            zone: CONSUMER_TAG\n";
        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);

        HttpHeaders headers = new HttpHeaders();

        // 测试命中group:CONSUMER_TAG的实例(consumer自身zone:az1)
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));
        }
    }

    /**
     * 测试tag匹配规则同AZ优先标签路由功能：规则含有TriggerThreshold和minAllInstances的policy
     * 大于minAllInstances和小于triggerThreshold场景
     * 非同AZ优先策略
     *
     * @param restCloudBasePath
     * @param feignBootBasePath
     * @param feignCloudBasePath
     * @throws InterruptedException
     */
    public void testPolicyAZRuleTwo(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        // 规则含有TriggerThreshold的policy
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-az-rule-trigger-threshold-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          zone:\n"
                + "            exact: 'az1'\n"
                + "            caseInsensitive: false\n"
                + "        policy:\n"
                + "          triggerThreshold: 90\n"
                + "          minAllInstances: 2\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            zone: CONSUMER_TAG\n";
        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);

        HttpHeaders headers = new HttpHeaders();

        // 测试命中group:CONSUMER_TAG的实例(consumer自身zone:az1)
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        int restCloudAZ1 = 0, restCloudAZ2 = 0;
        int feignBootAZ1 = 0, feignBootAZ2 = 0;
        int feignCloudAZ1 = 0, feignCloudAZ2 = 0;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            if (Objects.requireNonNull(exchange.getBody()).contains("az1")) {
                restCloudAZ1++;
            } else if (Objects.requireNonNull(exchange.getBody()).contains("az2")) {
                restCloudAZ2++;
            }

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            if (Objects.requireNonNull(exchange.getBody()).contains("az1")) {
                feignBootAZ1++;
            } else if (Objects.requireNonNull(exchange.getBody()).contains("az2")) {
                feignBootAZ2++;
            }

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            if (Objects.requireNonNull(exchange.getBody()).contains("az1")) {
                feignCloudAZ1++;
            } else if (Objects.requireNonNull(exchange.getBody()).contains("az2")) {
                feignCloudAZ2++;
            }
        }
        Assertions.assertNotEquals(0, restCloudAZ1);
        Assertions.assertNotEquals(0, restCloudAZ2);
        Assertions.assertNotEquals(0, feignBootAZ1);
        Assertions.assertNotEquals(0, feignBootAZ2);
        Assertions.assertNotEquals(0, feignCloudAZ1);
        Assertions.assertNotEquals(0, feignCloudAZ2);
    }

    /**
     * 测试tag匹配规则同AZ优先标签路由功能：规则含有TriggerThreshold和minAllInstances的policy
     * 小于minAllInstances和大于triggerThreshold场景
     * 同AZ优先策略
     *
     * @param restCloudBasePath
     * @param feignBootBasePath
     * @param feignCloudBasePath
     * @throws InterruptedException
     */
    public void testPolicyAZRuleThree(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        // 规则含有TriggerThreshold的policy
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-az-rule-trigger-threshold-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          zone:\n"
                + "            exact: 'az1'\n"
                + "            caseInsensitive: false\n"
                + "        policy:\n"
                + "          triggerThreshold: 40\n"
                + "          minAllInstances: 10\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            zone: CONSUMER_TAG\n";
        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);

        HttpHeaders headers = new HttpHeaders();

        // 测试命中group:CONSUMER_TAG的实例(consumer自身zone:az1)
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));
        }
    }

    /**
     * 测试tag匹配规则同AZ优先标签路由功能：规则含有TriggerThreshold和minAllInstances的policy
     * 小于minAllInstances和小于triggerThreshold场景
     * 同AZ优先策略
     *
     * @param restCloudBasePath
     * @param feignBootBasePath
     * @param feignCloudBasePath
     * @throws InterruptedException
     */
    public void testPolicyAZRuleFour(String restCloudBasePath, String feignBootBasePath, String feignCloudBasePath) throws InterruptedException {
        // 规则含有TriggerThreshold的policy
        String CONTENT = "---\n"
                + "- kind: routematcher.sermant.io/tag\n"
                + "  description: tag-az-rule-trigger-threshold-test\n"
                + "  rules:\n"
                + "    - precedence: 1\n"
                + "      match:\n"
                + "        tags:\n"
                + "          zone:\n"
                + "            exact: 'az1'\n"
                + "            caseInsensitive: false\n"
                + "        policy:\n"
                + "          triggerThreshold: 90\n"
                + "          minAllInstances: 10\n"
                + "      route:\n"
                + "        - tags:\n"
                + "            zone: CONSUMER_TAG\n";
        Assertions.assertTrue(KIE_CLIENT.publishConfig(REST_KEY, CONTENT));
        Assertions.assertTrue(KIE_CLIENT.publishConfig(FEIGN_KEY, CONTENT));
        TimeUnit.SECONDS.sleep(3);

        HttpHeaders headers = new HttpHeaders();

        // 测试命中group:CONSUMER_TAG的实例(consumer自身zone:az1)
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<String> exchange;
        for (int i = 0; i < TIMES; i++) {
            exchange = REST_TEMPLATE.exchange(restCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));

            exchange = REST_TEMPLATE.exchange(feignBootBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));

            exchange = REST_TEMPLATE.exchange(feignCloudBasePath, HttpMethod.GET, entity, String.class);
            Assertions.assertTrue(Objects.requireNonNull(exchange.getBody()).contains("az1"));
        }
    }


    private void clearConfig() throws InterruptedException {
        KIE_CLIENT.deleteKey(REST_KEY);
        KIE_CLIENT.deleteKey(FEIGN_KEY);
        TimeUnit.SECONDS.sleep(3);
    }

}
