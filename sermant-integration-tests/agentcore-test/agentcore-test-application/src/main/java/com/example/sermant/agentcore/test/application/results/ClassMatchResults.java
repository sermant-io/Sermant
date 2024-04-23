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

package com.example.sermant.agentcore.test.application.results;

/**
 * 类匹配测试用例结果
 *
 * @author tangle
 * @since 2023-10-18
 */
public enum ClassMatchResults {
    /**
     * 单一注解匹配
     */
    MATCHER_CLASS_BY_ANNOTATION("Test matcher class by single-annotation."),

    /**
     * 多注解匹配
     */
    MATCHER_CLASS_BY_ANNOTATIONS("Test matcher class by multi-annotation."),

    /**
     * 类名前缀匹配
     */
    MATCHER_CLASS_BY_CLASS_NAME_PREFIX("Test matcher class by the class-name's prefix."),

    /**
     * 类名中缀匹配
     */
    MATCHER_CLASS_BY_CLASS_NAME_INFIX("Test matcher class by the class-name's infix."),

    /**
     * 类名后缀匹配
     */
    MATCHER_CLASS_BY_CLASS_NAME_SUFFIX("Test matcher class by the class-name's suffix."),

    /**
     * 单一父类匹配
     */
    MATCHER_CLASS_BY_SUPER_TYPE("Test matcher class by single-superType."),

    /**
     * 超类匹配
     */
    MATCHER_CLASS_BY_SUPER_TYPES("Test matcher class by multi-superType.");

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
    ClassMatchResults(String description) {
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
