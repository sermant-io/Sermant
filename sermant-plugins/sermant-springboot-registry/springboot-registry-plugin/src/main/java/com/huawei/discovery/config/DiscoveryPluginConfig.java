/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.config;

import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

/**
 * 域名、日志打印等配置
 *
 * @author chengyouling
 * @since 2022-10-10
 */
@ConfigTypeKey("sermant.springboot.registry")
public class DiscoveryPluginConfig implements PluginConfig {

    /**
     * 拦截的域名
     */
    private String realmName;

    /**
     * 是否打印统计日志
     */
    private boolean loggerFlag = false;

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public boolean isLoggerFlag() {
        return loggerFlag;
    }

    public void setLoggerFlag(boolean loggerFlag) {
        this.loggerFlag = loggerFlag;
    }
}
