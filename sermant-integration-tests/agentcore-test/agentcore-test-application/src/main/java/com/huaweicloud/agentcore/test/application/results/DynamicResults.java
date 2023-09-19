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

package com.huaweicloud.agentcore.test.application.results;

/**
 * 动态安装卸载测试用例结果
 *
 * @author tangle
 * @since 2023-09-08
 */
public enum DynamicResults {
    /**
     * 动态安装插件重复增强不干扰
     */
    DYNAMIC_INSTALL_PLUGIN_REPEAT_ENHANCE("Test dynamic install plugin repetitive enhancement."),

    /**
     * 动态卸载插件，拦截点失效
     */
    DYNAMIC_UNINSTALL_PLUGIN_INTERCEPTOR_FAILURE("Test dynamic uninstall plugin, plugin failure."),

    /**
     * 动态卸载插件，服务关闭
     */
    DYNAMIC_UNINSTALL_SERVICE_CLOSE("Test dynamic uninstall plugin service close."),

    /**
     * 动态卸载插件，对已有拦截点不影响
     */
    DYNAMIC_UNINSTALL_REPEAT_ENHANCE("Test dynamic uninstall plugin not effect other interceptor."),

    /**
     * 动态卸载AGENT，插件失效
     */
    DYNAMIC_UNINSTALL_AGENT_PLUGIN_FAILURE("Test dynamic uninstall, plugin failure."),

    /**
     * 再次安装AGENT，插件生效
     */
    DYNAMIC_REINSTALL_AGENT_PLUGIN_SUCCESS("Test dynamic reinstall agent, plugin success.");

    /**
     * 用例描述
     */
    private String description;

    /**
     * 测试结果标识
     */
    private boolean result;

    /**
     * 构造函数
     *
     * @param description 用例描述
     */
    DynamicResults(String description) {
        this.description = description;
        this.result = true;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getDescription() {
        return description;
    }
}
