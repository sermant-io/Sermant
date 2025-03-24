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
 * Test results for class matching
 *
 * @author tangle
 * @since 2023-10-18
 */
public enum ClassMatchResults {
    /**
     * Single annotation matching
     */
    MATCHER_CLASS_BY_ANNOTATION("Test matcher class by single-annotation."),

    /**
     * Multi annotations matching
     */
    MATCHER_CLASS_BY_ANNOTATIONS("Test matcher class by multi-annotation."),

    /**
     * Class name prefix matching
     */
    MATCHER_CLASS_BY_CLASS_NAME_PREFIX("Test matcher class by the class-name's prefix."),

    /**
     * Class name infix matching
     */
    MATCHER_CLASS_BY_CLASS_NAME_INFIX("Test matcher class by the class-name's infix."),

    /**
     * Class name suffix matching
     */
    MATCHER_CLASS_BY_CLASS_NAME_SUFFIX("Test matcher class by the class-name's suffix."),

    /**
     * Single super type matching
     */
    MATCHER_CLASS_BY_SUPER_TYPE("Test matcher class by single-superType."),

    /**
     * Multiple super types matching
     */
    MATCHER_CLASS_BY_SUPER_TYPES("Test matcher class by multi-superType.");

    /**
     * Test case description
     */
    private String description;

    /**
     * Test result flag
     */
    private boolean result;

    /**
     * Constructor
     *
     * @param description Test case description
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
