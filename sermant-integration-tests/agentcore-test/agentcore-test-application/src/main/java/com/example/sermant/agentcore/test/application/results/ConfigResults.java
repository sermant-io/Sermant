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
 * Test results for core and plugin configuration loading
 *
 * @author tangle
 * @since 2023-10-09
 */
public enum ConfigResults {
    /**
     * Plugin configuration loaded successfully
     */
    PLUGIN_CONFIG_LOADED_SUCCESS("Test load plugin config."),

    /**
     * Core configuration loaded successfully
     */
    CORE_CONFIG_LOADED_SUCCESS("Test load core config.");

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
    ConfigResults(String description) {
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
