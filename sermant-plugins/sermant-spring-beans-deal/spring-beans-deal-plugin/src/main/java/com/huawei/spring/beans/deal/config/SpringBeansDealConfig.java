/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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
 * Remove the bean-related configuration in the spring container
 *
 * @author chengyouling
 * @since 2023-03-27
 */
@ConfigTypeKey("spring.beans.plugin")
public class SpringBeansDealConfig implements PluginConfig {
    /**
     * Whether to enable adaptation
     */
    private boolean enabled;

    /**
     * The name of the bean full path for the auto-assembly
     */
    private String excludeAutoConfigurations;

    /**
     * The full path name of the bean assembled by Component annotation
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
