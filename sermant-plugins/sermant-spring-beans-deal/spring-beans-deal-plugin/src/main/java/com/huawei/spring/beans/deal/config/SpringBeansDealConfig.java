/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.spring.beans.deal.config;

import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

/**
 * 剔除spring容器中bean相关配置
 *
 * @author chengyouling
 * @since 2023-03-27
 */
@ConfigTypeKey("spring.beans.plugin")
public class SpringBeansDealConfig implements PluginConfig {
    /**
     * 是否开启适配
     */
    private boolean enabled;

    /**
     * 自动装配的bean全路径名称
     */
    private String excludeAutoConfigurations;

    /**
     * Component注解装配的bean全路径名称
     */
    private String excludeBeans;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExcludeAutoConfigurations() {
        return excludeAutoConfigurations;
    }

    public void setExcludeAutoConfigurations(String excludeAutoConfigurations) {
        this.excludeAutoConfigurations = excludeAutoConfigurations;
    }

    public String getExcludeBeans() {
        return excludeBeans;
    }

    public void setExcludeBeans(String excludeBeans) {
        this.excludeBeans = excludeBeans;
    }
}
