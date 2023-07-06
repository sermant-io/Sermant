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

package com.huaweicloud.sermant.cache;

import com.huaweicloud.sermant.config.RemovalConfig;
import com.huaweicloud.sermant.config.RemovalRule;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Optional;

/**
 * 规则缓存类
 *
 * @author zhp
 * @since 2023-02-17
 */
public class RuleCacheTest {
    private static final float SCALE = 0.6f;

    private static final float RATE = 0.6f;

    private static final int NUM = 1;

    private static final String KEY = "SERVICE-A";

    private static MockedStatic<ConfigManager> configManagerMockedStatic;

    @BeforeClass
    public static void setUp() {
        RemovalConfig removalConfig = new RemovalConfig();
        RemovalRule removalRule = new RemovalRule();
        removalRule.setScaleUpLimit(SCALE);
        removalRule.setMinInstanceNum(NUM);
        removalRule.setErrorRate(RATE);
        removalRule.setKey(KEY);
        removalConfig.setRules(new ArrayList<>());
        removalConfig.getRules().add(removalRule);
        configManagerMockedStatic = Mockito.mockStatic(ConfigManager.class);
        configManagerMockedStatic.when(() -> PluginConfigManager.getPluginConfig(RemovalConfig.class)).thenReturn(removalConfig);
    }

    @Test
    public void testRule() {
        Optional<RemovalRule> removalRuleOptional = RuleCache.getRule(KEY);
        Assert.assertTrue(removalRuleOptional.isPresent());
        RemovalRule rule = removalRuleOptional.get();
        Assert.assertEquals(RATE, rule.getErrorRate(), 0.0);
        Assert.assertEquals(rule.getScaleUpLimit(), SCALE, 0.0);
        Assert.assertEquals(rule.getMinInstanceNum(), NUM);
    }

    @AfterClass
    public static void setDown() {
        if (configManagerMockedStatic != null) {
            configManagerMockedStatic.close();
        }
        InstanceCache.INSTANCE_MAP.clear();
    }
}