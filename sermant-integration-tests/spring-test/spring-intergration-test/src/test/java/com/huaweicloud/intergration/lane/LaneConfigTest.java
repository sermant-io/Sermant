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

package com.huaweicloud.intergration.lane;

import com.huaweicloud.intergration.config.supprt.KieClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 增加泳道配置
 *
 * @author provenceee
 * @since 2023-03-06
 */
@EnabledIfSystemProperty(named = "sermant.integration.test.type", matches = "LANE_CONFIG")
public class LaneConfigTest {
    private static final String REST_PROVIDER_KEY = "servicecomb.routeRule.rest-provider";

    private static final String FEIGN_PROVIDER_KEY = "servicecomb.routeRule.feign-provider";

    private static final String REST_CONSUMER_KEY = "servicecomb.routeRule.rest-consumer";

    private static final String FEIGN_CONSUMER_KEY = "servicecomb.routeRule.feign-consumer";

    private static final String ZUUL_KEY = "servicecomb.routeRule.spring-zuul";

    private static final String GATEWAY_KEY = "servicecomb.routeRule.spring-gateway";

    private final KieClient kieClient;

    public LaneConfigTest() {
        Map<String, String> map = new HashMap<>();
        map.put("app", "lane");
        map.put("environment", "development");
        kieClient = new KieClient(new RestTemplate(), null, map);
    }

    /**
     * 添加路由配置
     */
    @Test
    public void addGatewayConfig() {
        String content = "---\n"
            + "- kind: route.sermant.io/lane\n"
            + "  description: lane\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        method: get\n"
            + "        path: \"/rest/router/cloud/getLane\"\n"
            + "        protocol: http\n"
            + "        headers:\n"
            + "          x-user-id:\n"
            + "            greater: 100\n"
            + "            caseInsensitive: false\n"
            + "        parameters:\n"
            + "          name:\n"
            + "            in: [BaR]\n"
            + "            caseInsensitive: true\n"
            + "          id:\n"
            + "            less: 10\n"
            + "          enabled:\n"
            + "            exact: true\n"
            + "      route:\n"
            + "        - tag-inject:\n"
            + "            x-sermant-flag1: gray1\n"
            + "            x-sermant-flag2: gray2\n"
            + "          weight: 100\n"
            + "    - precedence: 2\n"
            + "      match:\n"
            + "        method: get\n"
            + "        path: \"/feign/router/cloud/getLane\"\n"
            + "        protocol: http\n"
            + "        headers:\n"
            + "          x-user-id:\n"
            + "            greater: 100\n"
            + "            caseInsensitive: false\n"
            + "        parameters:\n"
            + "          name:\n"
            + "            in: [BaR]\n"
            + "            caseInsensitive: true\n"
            + "          id:\n"
            + "            less: 10\n"
            + "          enabled:\n"
            + "            exact: true\n"
            + "      route:\n"
            + "        - tag-inject:\n"
            + "            x-sermant-flag3: gray3\n"
            + "            x-sermant-flag4: gray4\n"
            + "          weight: 100";
        Assertions.assertTrue(kieClient.publishConfig(ZUUL_KEY, content));
        Assertions.assertTrue(kieClient.publishConfig(GATEWAY_KEY, content));
    }

    /**
     * 添加路由配置
     */
    @Test
    public void addConsumerConfig() {
        String restContent = "---\n"
            + "- kind: route.sermant.io/lane\n"
            + "  description: consumer-lane\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        method: get\n"
            + "        path: \"/router/cloud/getLane\"\n"
            + "        protocol: http\n"
            + "        headers:\n"
            + "          x-user-id:\n"
            + "            less: 100\n"
            + "            caseInsensitive: false\n"
            + "        parameters:\n"
            + "          name:\n"
            + "            in: [BaR]\n"
            + "            caseInsensitive: true\n"
            + "          id:\n"
            + "            greater: 10\n"
            + "          enabled:\n"
            + "            exact: true\n"
            + "      route:\n"
            + "        - tag-inject:\n"
            + "            x-sermant-flag5: gray5\n"
            + "          weight: 100\n"
            + "- kind: routematcher.sermant.io/flow\n"
            + "  discription: consumer-flow\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        headers:\n"
            + "          x-sermant-flag1:\n"
            + "            exact: gray1\n"
            + "      route:\n"
            + "        - weight: 100\n"
            + "          tags:\n"
            + "            version: 1.0.1\n"
            + "    - precedence: 2\n"
            + "      match:\n"
            + "        headers:\n"
            + "          x-sermant-flag3:\n"
            + "            exact: gray3\n"
            + "      route:\n"
            + "        - weight: 100\n"
            + "          tags:\n"
            + "            version: 1.0.1";
        Assertions.assertTrue(kieClient.publishConfig(REST_CONSUMER_KEY, restContent));

        String feignContent = "---\n"
            + "- kind: route.sermant.io/lane\n"
            + "  description: consumer-lane\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        method: get\n"
            + "        path: \"/router/cloud/getLane\"\n"
            + "        protocol: http\n"
            + "        headers:\n"
            + "          x-user-id:\n"
            + "            less: 100\n"
            + "            caseInsensitive: false\n"
            + "        parameters:\n"
            + "          name:\n"
            + "            in: [FOO]\n"
            + "          id:\n"
            + "            less: 10\n"
            + "          enabled:\n"
            + "            exact: true\n"
            + "      route:\n"
            + "        - tag-inject:\n"
            + "            x-sermant-flag6: gray6\n"
            + "          weight: 100\n"
            + "- kind: routematcher.sermant.io/flow\n"
            + "  discription: consumer-flow\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        headers:\n"
            + "          x-sermant-flag1:\n"
            + "            exact: gray1\n"
            + "      route:\n"
            + "        - weight: 100\n"
            + "          tags:\n"
            + "            version: 1.0.1\n"
            + "    - precedence: 2\n"
            + "      match:\n"
            + "        headers:\n"
            + "          x-sermant-flag3:\n"
            + "            exact: gray3\n"
            + "      route:\n"
            + "        - weight: 100\n"
            + "          tags:\n"
            + "            version: 1.0.1";
        Assertions.assertTrue(kieClient.publishConfig(FEIGN_CONSUMER_KEY, feignContent));
    }

    /**
     * 添加路由配置
     */
    @Test
    public void addProviderConfig() {
        String restContent = "---\n"
            + "- kind: routematcher.sermant.io/flow\n"
            + "  discription: provider-flow\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        headers:\n"
            + "          x-sermant-flag5:\n"
            + "            exact: gray5\n"
            + "      route:\n"
            + "        - weight: 100\n"
            + "          tags:\n"
            + "            version: 1.0.1";
        Assertions.assertTrue(kieClient.publishConfig(REST_PROVIDER_KEY, restContent));

        String feignContent = "---\n"
            + "- kind: routematcher.sermant.io/flow\n"
            + "  discription: provider-flow\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        headers:\n"
            + "          x-sermant-flag6:\n"
            + "            exact: gray6\n"
            + "      route:\n"
            + "        - weight: 100\n"
            + "          tags:\n"
            + "            version: 1.0.1";
        Assertions.assertTrue(kieClient.publishConfig(FEIGN_PROVIDER_KEY, feignContent));
    }
}