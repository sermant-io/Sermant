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

package com.huaweicloud.sermant.router.config.service;

import com.huaweicloud.sermant.router.config.entity.Match;
import com.huaweicloud.sermant.router.config.entity.MatchRule;
import com.huaweicloud.sermant.router.config.entity.RouterConfiguration;
import com.huaweicloud.sermant.router.config.entity.Rule;
import com.huaweicloud.sermant.router.config.utils.RuleUtils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

/**
 * 测试ConfigService
 *
 * @author provenceee
 * @since 2022-09-13
 */
public class ConfigServiceTest {

    /**
     * 测试getMatchKeys方法
     */
    @Test
    public void testGetMatchKeys() {
        Match match = new Match();
        match.setHeaders(Collections.singletonMap("bar", Collections.singletonList(new MatchRule())));
        Rule rule = new Rule();
        rule.setMatch(match);
        RuleUtils.updateMatchKeys("bar", Collections.singletonList(rule));
        DubboConfigServiceImpl dubboConfigService = new DubboConfigServiceImpl();
        Set<String> headerKeys = dubboConfigService.getMatchKeys();
        Assert.assertEquals(1, headerKeys.size());

        // 清除缓存
        RuleUtils.initMatchKeys(new RouterConfiguration());

        RuleUtils.updateMatchKeys("bar", Collections.singletonList(rule));
        SpringConfigServiceImpl springConfigService = new SpringConfigServiceImpl();
        headerKeys = springConfigService.getMatchKeys();
        Assert.assertEquals(1, headerKeys.size());
    }
}