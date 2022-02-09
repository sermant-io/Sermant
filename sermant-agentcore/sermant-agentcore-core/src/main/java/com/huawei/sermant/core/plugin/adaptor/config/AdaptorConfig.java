/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.plugin.adaptor.config;

import com.huawei.sermant.core.config.common.BaseConfig;
import com.huawei.sermant.core.config.common.ConfigTypeKey;

/**
 * 适配器配置
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-26
 */
@ConfigTypeKey("adaptor.config")
public class AdaptorConfig implements BaseConfig {
    /**
     * 是否加载适配器
     */
    private boolean isLoadExtAgentEnable = false;

    /**
     * 适配器的全局运行环境目录
     */
    private String executeEnvDir;

    public boolean isLoadExtAgentEnable() {
        return isLoadExtAgentEnable;
    }

    @SuppressWarnings("checkstyle:RegexpSingleline")
    public void setLoadExtAgentEnable(boolean loadExtAgentEnable) {
        isLoadExtAgentEnable = loadExtAgentEnable;
    }

    public String getExecuteEnvDir() {
        return executeEnvDir;
    }

    public void setExecuteEnvDir(String executeEnvDir) {
        this.executeEnvDir = executeEnvDir;
    }
}
