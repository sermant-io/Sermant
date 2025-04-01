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
 * Test results for dynamic configuration
 *
 * @author tangle
 * @since 2023-09-08
 */
public enum DynamicConfigResults {
    /**
     * Publishes dynamic configuration
     */
    DYNAMIC_PUBLISH_CONFIG("Test publish dynamic config."),

    /**
     * Removes dynamic configuration
     */
    DYNAMIC_REMOVE_CONFIG("Test remove dynamic config."),

    /**
     * Adds single dynamic configuration listener
     */
    DYNAMIC_ADD_CONFIG_LISTENER("Test add dynamic config listener."),

    /**
     * Removes single dynamic configuration listener
     */
    DYNAMIC_REMOVE_CONFIG_LISTENER("Test remove dynamic config listener."),

    /**
     * Adds group dynamic configuration listener
     */
    DYNAMIC_ADD_GROUP_CONFIG_LISTENER("Test add group dynamic config listener."),

    /**
     * Removes group dynamic configuration listener
     */
    DYNAMIC_REMOVE_GROUP_CONFIG_LISTENER("Test remove group dynamic config listener.");

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
