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

package com.huaweicloud.agentcore.test.application.controller;

import com.huaweicloud.agentcore.test.application.results.ConfigResults;
import com.huaweicloud.agentcore.test.application.results.DynamicConfigResults;
import com.huaweicloud.agentcore.test.application.results.DynamicResults;
import com.huaweicloud.agentcore.test.application.tests.configload.CoreAndPluginConfigLoadTest;
import com.huaweicloud.agentcore.test.application.tests.dynamic.DynamicTest;
import com.huaweicloud.agentcore.test.application.tests.dynamicconfig.DynamicConfigTest;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * http接口方法
 *
 * @author tangle
 * @since 2023-09-08
 */
public class TestController {
    /**
     * 作为服务运行成功的请求判断接口
     *
     * @return “OK”
     */
    public String ping() {
        return "OK";
    }

    /**
     * 测试动态配置
     *
     * @return 测试结果
     */
    public String testDynamicConfig() {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            DynamicConfigTest dynamicConfigTest = new DynamicConfigTest();
            dynamicConfigTest.testDynamicConfig();
            for (DynamicConfigResults value : DynamicConfigResults.values()) {
                resultMap.put(value.name(), value.getResult());
            }
        } catch (InterruptedException exception) {
            resultMap.put(buildExceptionKey(exception), false);
        }
        JSONObject jsonObject = new JSONObject(resultMap);
        return jsonObject.toJSONString();
    }

    /**
     * 测试动态安装插件
     *
     * @return 测试结果
     */
    public String testInstallPlugin() {
        Map<String, Object> resultMap = new HashMap<>();
        DynamicTest dynamicTest = new DynamicTest();
        dynamicTest.testInstallPlugin();
        resultMap.put(DynamicResults.DYNAMIC_INSTALL_PLUGIN_REPEAT_ENHANCE.name(),
                DynamicResults.DYNAMIC_INSTALL_PLUGIN_REPEAT_ENHANCE.getResult());
        JSONObject jsonObject = new JSONObject(resultMap);
        return jsonObject.toJSONString();
    }

    /**
     * 测试动态卸载插件
     *
     * @return 测试结果
     */
    public String testUninstallPlugin() {
        Map<String, Object> resultMap = new HashMap<>();
        DynamicTest dynamicTest = new DynamicTest();
        dynamicTest.testUninstallPlugin();
        resultMap.put(DynamicResults.DYNAMIC_UNINSTALL_PLUGIN_INTERCEPTOR_FAILURE.name(),
                DynamicResults.DYNAMIC_UNINSTALL_PLUGIN_INTERCEPTOR_FAILURE.getResult());
        resultMap.put(DynamicResults.DYNAMIC_UNINSTALL_REPEAT_ENHANCE.name(),
                DynamicResults.DYNAMIC_UNINSTALL_REPEAT_ENHANCE.getResult());
        JSONObject jsonObject = new JSONObject(resultMap);
        return jsonObject.toJSONString();
    }

    /**
     * 测试动态卸载agent
     *
     * @return 测试结果
     */
    public String testUninstallAgent() {
        Map<String, Object> resultMap = new HashMap<>();
        DynamicTest dynamicTest = new DynamicTest();
        dynamicTest.testUninstallAgent();
        resultMap.put(DynamicResults.DYNAMIC_UNINSTALL_AGENT_PLUGIN_FAILURE.name(),
                DynamicResults.DYNAMIC_UNINSTALL_AGENT_PLUGIN_FAILURE.getResult());
        JSONObject jsonObject = new JSONObject(resultMap);
        return jsonObject.toJSONString();
    }

    /**
     * 测试动态重装agent
     *
     * @return 测试结果
     */
    public String testReInstallAgent() {
        Map<String, Object> resultMap = new HashMap<>();
        DynamicTest dynamicTest = new DynamicTest();
        dynamicTest.testReInstallAgent();
        resultMap.put(DynamicResults.DYNAMIC_REINSTALL_AGENT_PLUGIN_SUCCESS.name(),
                DynamicResults.DYNAMIC_REINSTALL_AGENT_PLUGIN_SUCCESS.getResult());
        JSONObject jsonObject = new JSONObject(resultMap);
        return jsonObject.toJSONString();
    }

    /**
     * 测试premain启动
     *
     * @return 测试结果
     */
    public String testPremainStartup() {
        Map<String, Object> resultMap = new HashMap<>();
        DynamicTest dynamicTest = new DynamicTest();
        dynamicTest.testPremainStartup();
        resultMap.put(DynamicResults.PREMAIN_STATIC_PLUGIN_INTERCEPTOR_SUCCESS.name(),
                DynamicResults.PREMAIN_STATIC_PLUGIN_INTERCEPTOR_SUCCESS.getResult());
        resultMap.put(DynamicResults.PREMAIN_DYNAMIC_PLUGIN_INTERCEPTOR_FAILURE.name(),
                DynamicResults.PREMAIN_DYNAMIC_PLUGIN_INTERCEPTOR_FAILURE.getResult());
        JSONObject jsonObject = new JSONObject(resultMap);
        return jsonObject.toJSONString();
    }

    /**
     * 测试agentmain启动
     *
     * @return 测试结果
     */
    public String testAgentmainStartup() {
        Map<String, Object> resultMap = new HashMap<>();
        DynamicTest dynamicTest = new DynamicTest();
        dynamicTest.testAgentmainStartup();
        resultMap.put(DynamicResults.AGENTMAIN_STATIC_PLUGIN_INTERCEPTOR_FAILURE.name(),
                DynamicResults.AGENTMAIN_STATIC_PLUGIN_INTERCEPTOR_FAILURE.getResult());
        resultMap.put(DynamicResults.AGENTMAIN_ACTIVE_PLUGIN_INTERCEPTOR_SUCCESS.name(),
                DynamicResults.AGENTMAIN_ACTIVE_PLUGIN_INTERCEPTOR_SUCCESS.getResult());
        resultMap.put(DynamicResults.AGENTMAIN_PASSIVE_PLUGIN_INTERCEPTOR_FAILURE.name(),
                DynamicResults.AGENTMAIN_PASSIVE_PLUGIN_INTERCEPTOR_FAILURE.getResult());
        JSONObject jsonObject = new JSONObject(resultMap);
        return jsonObject.toJSONString();
    }

    /**
     * 测试配置加载功能
     *
     * @return 测试结果
     */
    public String testCoreAndPluginConfigLoad() {
        Map<String, Object> resultMap = new HashMap<>();
        CoreAndPluginConfigLoadTest configTest = new CoreAndPluginConfigLoadTest();
        configTest.testCoreAndPluginConfigLoad();
        resultMap.put(ConfigResults.PLUGIN_CONFIG_LOADED_SUCCESS.name(),
                ConfigResults.PLUGIN_CONFIG_LOADED_SUCCESS.getResult());
        resultMap.put(ConfigResults.CORE_CONFIG_LOADED_SUCCESS.name(),
                ConfigResults.CORE_CONFIG_LOADED_SUCCESS.getResult());
        JSONObject jsonObject = new JSONObject(resultMap);
        return jsonObject.toJSONString();
    }

    private String buildExceptionKey(Exception e) {
        return "Unexpected exception occurs: " + e.getMessage();
    }
}
