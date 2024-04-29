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

package io.sermant.removal.config;

import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;
import io.sermant.removal.common.RemovalConstants;

import java.util.List;

/**
 * Outlier instance removal configuration
 *
 * @author zhp
 * @since 2023-02-17
 */
@ConfigTypeKey("removal.config")
public class RemovalConfig implements PluginConfig {
    /**
     * The expiration time of the instance
     */
    private int expireTime;

    /**
     * Recovery time
     */
    private int recoveryTime;

    /**
     * Supported exception types
     */
    private List<String> exceptions;

    /**
     * The outlier instance removal switch
     */
    private boolean enableRemoval;

    /**
     * Window time
     */
    private int windowsTime = RemovalConstants.WINDOWS_TIME;

    /**
     * Number of windows
     */
    private int windowsNum = RemovalConstants.WINDOWS_NUM;

    private List<RemovalRule> rules;

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public int getRecoveryTime() {
        return recoveryTime;
    }

    public void setRecoveryTime(int recoveryTime) {
        this.recoveryTime = recoveryTime;
    }

    public int getWindowsTime() {
        return windowsTime;
    }

    public void setWindowsTime(int windowsTime) {
        this.windowsTime = windowsTime;
    }

    public List<String> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<String> exceptions) {
        this.exceptions = exceptions;
    }

    public boolean isEnableRemoval() {
        return enableRemoval;
    }

    public void setEnableRemoval(boolean enableRemoval) {
        this.enableRemoval = enableRemoval;
    }

    public int getWindowsNum() {
        return windowsNum;
    }

    public void setWindowsNum(int windowsNum) {
        this.windowsNum = windowsNum;
    }

    public List<RemovalRule> getRules() {
        return rules;
    }

    public void setRules(List<RemovalRule> rules) {
        this.rules = rules;
    }
}
