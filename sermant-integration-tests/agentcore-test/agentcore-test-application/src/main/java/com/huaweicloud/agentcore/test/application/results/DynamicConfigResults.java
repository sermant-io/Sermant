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
 * 动态配置测试用例结果
 *
 * @author tangle
 * @since 2023-09-08
 */
public enum DynamicConfigResults {
    /**
     * 动态配置发布配置
     */
    DYNAMIC_PUBLISH_CONFIG("Test publish dynamic config."),

    /**
     * 动态配置移除配置
     */
    DYNAMIC_REMOVE_CONFIG("Test remove dynamic config."),

    /**
     * 动态配置添加单一配置监听
     */
    DYNAMIC_ADD_CONFIG_LISTENER("Test add dynamic config listener."),

    /**
     * 动态配置移除单一配置监听
     */
    DYNAMIC_REMOVE_CONFIG_LISTENER("Test remove dynamic config listener."),

    /**
     * 动态配置添加组配置监听
     */
    DYNAMIC_ADD_GROUP_CONFIG_LISTENER("Test add group dynamic config listener."),

    /**
     * 动态配置移除组配置监听
     */
    DYNAMIC_REMOVE_GROUP_CONFIG_LISTENER("Test remove group dynamic config listener.");

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
    DynamicConfigResults(String description) {
        this.description = description;
        this.result = false;
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
