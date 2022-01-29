/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.sermant.core.service.dynamicconfig.kie.config;

import com.huawei.sermant.core.config.common.BaseConfig;
import com.huawei.sermant.core.config.common.ConfigTypeKey;

/**
 * KIE配置
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-12-15
 */
@ConfigTypeKey("kie.dynamic.config")
public class KieDynamicConfig implements BaseConfig {
    /**
     * 默认kie的命名空间
     */
    private String project = "default";

    public String getProject() {
        return project;
    }
}
