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

package com.huaweicloud.sermant.config;

import com.huaweicloud.sermant.common.RemovalConstants;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

import java.util.List;

/**
 * 离群实例摘除配置
 *
 * @author zhp
 * @since 2023-02-17
 */
@ConfigTypeKey("removal.config")
public class RemovalConfig implements PluginConfig {
    /**
     * 实例过期时间
     */
    private int expireTime;

    /**
     * 恢复时间
     */
    private int recoveryTime;

    /**
     * 支持的异常类型
     */
    private List<String> exceptions;

    /**
     * 离群实例摘除开关
     */
    private boolean enableRemoval;

    /**
     * 窗口时间
     */
    private int windowsTime = RemovalConstants.WINDOWS_TIME;

    /**
     * 窗口数量
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
