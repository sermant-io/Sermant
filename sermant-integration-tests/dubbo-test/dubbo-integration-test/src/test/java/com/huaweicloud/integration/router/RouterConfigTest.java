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

import com.huaweicloud.integration.support.KieClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestTemplate;

/**
 * 增加标签路由配置
 *
 * @author provenceee
 * @since 2022-11-07
 */
@EnabledIfEnvironmentVariable(named = "TEST_TYPE", matches = "router-config")
public class RouterConfigTest {
    private final RestTemplate restTemplate = new RestTemplate();

    private final KieClient kieClient = new KieClient(restTemplate);

    private static final String KEY = "servicecomb.routeRule.dubbo-integration-provider";

    private static final String CONTENT = "---\n"
        + "- precedence: 1\n"
        + "  match:\n"
        + "    headers:\n"
        + "        id:\n"
        + "          exact: '1'\n"
        + "          caseInsensitive: false\n"
        + "  route:\n"
        + "    - tags:\n"
        + "        group: gray\n"
        + "      weight: 100\n"
        + "- precedence: 2\n"
        + "  match:\n"
        + "    headers:\n"
        + "        name:\n"
        + "          exact: 'bar'\n"
        + "          caseInsensitive: false\n"
        + "  route:\n"
        + "    - tags:\n"
        + "        version: 1.0.1\n"
        + "      weight: 100";

    @Test
    public void addRouterConfig() {
        Assertions.assertTrue(kieClient.publishConfig(KEY, CONTENT));
    }
}
