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

package com.huaweicloud.sermant.router.config.entity;

import com.huaweicloud.sermant.router.common.constants.RouterConstant;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * RouterConfiguration单元测试
 *
 * @author lilai
 * @since 2023-02-28
 */
public class RouterConfigurationTest {
    private static RouterConfiguration routerConfiguration;

    @Before
    public void setUp() {
        routerConfiguration = new RouterConfiguration();
    }

    @Test
    public void testUpdateServiceRule() {
        List<EntireRule> entireRules = new ArrayList<>();
        EntireRule flowMatchRule = new EntireRule();
        flowMatchRule.setKind(RouterConstant.FLOW_MATCH_KIND);
        flowMatchRule.setDescription("flow match rule");
        flowMatchRule.setRules(new ArrayList<>());
        EntireRule tagMatchRule = new EntireRule();
        tagMatchRule.setKind(RouterConstant.TAG_MATCH_KIND);
        tagMatchRule.setDescription("tag match rule");
        tagMatchRule.setRules(new ArrayList<>());
        EntireRule laneMatchRule = new EntireRule();
        laneMatchRule.setKind(RouterConstant.LANE_MATCH_KIND);
        laneMatchRule.setDescription("lane match rule");
        laneMatchRule.setRules(new ArrayList<>());
        entireRules.add(flowMatchRule);
        entireRules.add(tagMatchRule);
        entireRules.add(laneMatchRule);
        routerConfiguration.updateServiceRule("testService", entireRules);
        Assert.assertEquals(3, routerConfiguration.getRouteRule().size());
    }
}
