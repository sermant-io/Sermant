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
 * 类的重转换能力测试用例结果
 *
 * @author tangle
 * @since 2023-10-18
 */
public enum ReTransformResults {
    /**
     * 无参构造方法增强
     */
    ENHANCE_NO_ARGUMENT_CONSTRUCTOR("Test enhance system class no-argument constructor."),

    /**
     * 静态方法增强
     */
    ENHANCE_STATIC_FUNCTION("Test enhance system class static function."),

    /**
     * 实例方法增强
     */
    ENHANCE_INSTANCE_FUNCTION("Test enhance system class instance function."),

    /**
     * 静态方法跳过
     */
    ENHANCE_STATIC_FUNCTION_SKIP("Test skip system class static function.");

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
    ReTransformResults(String description) {
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
