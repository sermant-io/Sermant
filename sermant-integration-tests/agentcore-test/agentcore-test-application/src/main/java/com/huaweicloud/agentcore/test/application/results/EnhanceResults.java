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

package com.huaweicloud.agentcore.test.application.results;

/**
 * 方法增强用例结果
 *
 * @author tangle
 * @since 2023-10-18
 */
public enum EnhanceResults {
    /**
     * 修改成员属性
     */
    MODIFY_MEMBER_FIELDS("Test modify the member fields of object."),

    /**
     * 修改静态属性
     */
    MODIFY_STATIC_FIELDS("Test modify the static fields of object."),

    /**
     * 修改入参
     */
    MODIFY_ARGUMENTS("Test modify the arguments of method."),

    /**
     * 修改返回值
     */
    MODIFY_RESULT("Test modify the result of method."),

    /**
     * 测试方法跳过
     */
    SKIP_METHOD("Test skip the method.");

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
    EnhanceResults(String description) {
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
