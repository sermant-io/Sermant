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
 * Method enhancement test case results
 *
 * @author tangle
 * @since 2023-10-18
 */
public enum EnhanceResults {
    /**
     * Modify member fields
     */
    MODIFY_MEMBER_FIELDS("Test modify the member fields of object."),

    /**
     * Modify static fields
     */
    MODIFY_STATIC_FIELDS("Test modify the static fields of object."),

    /**
     * Modify arguments
     */
    MODIFY_ARGUMENTS("Test modify the arguments of method."),

    /**
     * Modify return value
     */
    MODIFY_RESULT("Test modify the result of method."),

    /**
     * Skip method
     */
    SKIP_METHOD("Test skip the method.");

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
