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

package com.example.sermant.agentcore.test.application.tests.configload;

import com.example.sermant.agentcore.test.application.results.ConfigResults;

/**
 * 核心配置和插件配置加载测试类
 *
 * @author tangle
 * @since 2023-10-09
 */
public class CoreAndPluginConfigLoadTest {
    /**
     * 测试配置加载功能
     */
    public void testCoreAndPluginConfigLoad() {
        boolean[] result = checkConfig(false, false);
        ConfigResults.PLUGIN_CONFIG_LOADED_SUCCESS.setResult(result[0]);
        ConfigResults.CORE_CONFIG_LOADED_SUCCESS.setResult(result[1]);
    }

    /**
     * 测试插件的增强拦截方法
     *
     * @param pluginConfigFlag 插件配置增强flag
     * @param coreConfigFlag 核心配置增强flag
     * @return 增强结果数组
     */
    private boolean[] checkConfig(boolean pluginConfigFlag, boolean coreConfigFlag) {
        return new boolean[]{pluginConfigFlag, coreConfigFlag};
    }
}
