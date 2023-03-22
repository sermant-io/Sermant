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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

/**
 * 增加标签路由配置
 *
 * @author provenceee
 * @since 2022-11-07
 */
public class TagRouterConfigTest {
    private final RestTemplate restTemplate = new RestTemplate();

    private final KieClient kieClient = new KieClient(restTemplate);

    private static final String REST_KEY = "servicecomb.routeRule.rest-provider";

    private static final String FEIGN_KEY = "servicecomb.routeRule.feign-provider";

    private static final String STRATEGY_KEY = "sermant.plugin.registry";

    private static final String STRATEGY_VALUE = "strategy: all";

    private static final String CONTENT = "---\n"
            + "kind: routematcher.sermant.io/flow\n"
            + "description: flow-rule-test\n"
            + "rules:\n"
            + "  - precedence: 1\n"
            + "    match:\n"
            + "      headers:\n"
            + "        id:\n"
            + "          exact: '1'\n"
            + "          caseInsensitive: false\n"
            + "    route:\n"
            + "      - tags:\n"
            + "          group: gray\n"
            + "        weight: 100\n"
            + "  - precedence: 2\n"
            + "    match:\n"
            + "      headers:\n"
            + "        name:\n"
            + "          exact: 'bar'\n"
            + "          caseInsensitive: false\n"
            + "    route:\n"
            + "      - tags:\n"
            + "          version: 1.0.1\n"
            + "        weight: 100";

    @Rule
    public final TagRouterConfigRule rule = new TagRouterConfigRule();

    /**
     * 添加路由配置
     */
    @Test
    public void addRouterConfig() {
        Assert.assertTrue(kieClient.publishConfig(REST_KEY, CONTENT));
        Assert.assertTrue(kieClient.publishConfig(FEIGN_KEY, CONTENT));
    }

    /**
     * 添加注册插件灰度配置
     */
    @Test
    public void addStrategyConfig() {
        Assert.assertTrue(kieClient.publishConfig(STRATEGY_KEY, STRATEGY_VALUE));
    }
}
