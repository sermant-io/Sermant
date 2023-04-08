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
    private int expireTimes;

    /**
     * 恢复时间
     */
    private int recoveryTimes;

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
    private int windowsTimes;

    /**
     * 窗口数量
     */
    private int windowsNum;

    private List<RemovalRule> rules;

    public int getExpireTimes() {
        return expireTimes;
    }

    public void setExpireTimes(int expireTimes) {
        this.expireTimes = expireTimes;
    }

    public int getRecoveryTimes() {
        return recoveryTimes;
    }

    public void setRecoveryTimes(int recoveryTimes) {
        this.recoveryTimes = recoveryTimes;
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

    public int getWindowsTimes() {
        return windowsTimes;
    }

    public void setWindowsTimes(int windowsTimes) {
        this.windowsTimes = windowsTimes;
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
