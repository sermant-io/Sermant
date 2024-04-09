/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.agentcore.tests.plugin.config;

import com.huaweicloud.sermant.core.config.common.ConfigFieldKey;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

/**
 * 测试配置
 *
 * @author tangle
 * @since 2023-10-09
 */
@ConfigTypeKey("configload.config")
public class TestConfig implements PluginConfig {
    @ConfigFieldKey("test-config-value")
    private String testConfigValue;

    public String getTestConfigValue() {
        return testConfigValue;
    }

    public void setTestConfigValue(String testConfigValue) {
        this.testConfigValue = testConfigValue;
    }
}

