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

package com.huaweicloud.integration.lane;

import com.huaweicloud.integration.support.KieClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 增加泳道测试的配置
 *
 * @author provenceee
 * @since 2023-03-02
 */
@EnabledIfEnvironmentVariable(named = "TEST_TYPE", matches = "lane-config")
public class LaneConfigTest {
    private final KieClient kieClient;

    public LaneConfigTest() {
        Map<String, String> map = new HashMap<>();
        map.put("app", "lane");
        map.put("environment", "development");
        kieClient = new KieClient(new RestTemplate(), null, map);
    }

    @Test
    public void addControllerLaneConfig() {
        String content = "---\n"
            + "- kind: route.sermant.io/lane\n"
            + "  description: controller-lane\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        method: get\n"
            + "        path: \"^/controller/getLaneBy.*\"\n"
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
            + "          weight: 100";
        Assertions.assertTrue(kieClient.publishConfig("servicecomb.routeRule.dubbo-integration-controller", content));
    }

    @Test
    public void addConsumerLaneConfig() {
        String content = "---\n"
            + "- kind: route.sermant.io/lane\n"
            + "  description: consumer-lane\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        method: getLaneByDubbo\n"
            + "        path: \"com.huaweicloud.integration.service.LaneService\"\n"
            + "        protocol: dubbo\n"
            + "        attachments:\n"
            + "          x-user-id:\n"
            + "            noless: 100\n"
            + "        args:\n"
            + "          args0:\n"
            + "            noequ: FOO\n"
            + "            caseInsensitive: true\n"
            + "          args1:\n"
            + "            nogreater: 10\n"
            + "            type: .id\n"
            + "          args2:\n"
            + "            regex: \"^foo.*\"\n"
            + "            type: \"[0]\"\n"
            + "          args3:\n"
            + "            exact: 100\n"
            + "            type: .get(0)\n"
            + "          args4:\n"
            + "            exact: \"baR\"\n"
            + "            type: .get(\"name\")\n"
            + "      route:\n"
            + "        - tag-inject:\n"
            + "            x-sermant-flag3: gray3\n"
            + "            x-sermant-flag4: gray4\n"
            + "          weight: 100\n"
            + "    - precedence: 2\n"
            + "      match:\n"
            + "        path: \"com.huaweicloud.integration.service.LaneService\"\n"
            + "        protocol: dubbo\n"
            + "        attachments:\n"
            + "          x-user-id:\n"
            + "            noless: 100\n"
            + "        args:\n"
            + "          args1:\n"
            + "            exact: true\n"
            + "            type: .isEnabled()\n"
            + "      route:\n"
            + "        - tag-inject:\n"
            + "            x-sermant-flag5: gray5\n"
            + "            x-sermant-flag6: gray6\n"
            + "          weight: 100\n"
            + "- kind: routematcher.sermant.io/flow\n"
            + "  discription: flow\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        attachments:\n"
            + "          x-sermant-flag1:\n"
            + "            exact: gray1\n"
            + "      route:\n"
            + "        - weight: 100\n"
            + "          tags:\n"
            + "            version: 1.0.1";
        Assertions.assertTrue(kieClient.publishConfig("servicecomb.routeRule.dubbo-integration-consumer", content));
    }

    @Test
    public void addProviderLaneConfig() {
        String content = "---\n"
            + "- kind: routematcher.sermant.io/flow\n"
            + "  discription: provider-flow\n"
            + "  rules:\n"
            + "    - precedence: 1\n"
            + "      match:\n"
            + "        headers:\n"
            + "          x-sermant-flag5:\n"
            + "            exact: gray5\n"
            + "        attachments:\n"
            + "          x-sermant-flag5:\n"
            + "            exact: gray5\n"
            + "      route:\n"
            + "        - weight: 100\n"
            + "          tags:\n"
            + "            version: 1.0.1\n"
            + "    - precedence: 2\n"
            + "      match:\n"
            + "        headers:\n"
            + "          x-sermant-flag4:\n"
            + "            exact: gray4\n"
            + "        attachments:\n"
            + "          x-sermant-flag4:\n"
            + "            exact: gray4\n"
            + "      route:\n"
            + "        - weight: 100\n"
            + "          tags:\n"
            + "            version: 1.0.1";
        Assertions.assertTrue(kieClient.publishConfig("servicecomb.routeRule.dubbo-integration-provider", content));
    }
}