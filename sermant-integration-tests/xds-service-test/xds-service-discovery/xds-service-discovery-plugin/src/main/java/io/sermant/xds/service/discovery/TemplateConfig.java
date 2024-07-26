/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.xds.service.discovery;

import io.sermant.core.config.common.ConfigFieldKey;
import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;

/**
 * config
 *
 * @author daizhenyu
 * @since 2024-07-02
 **/
@ConfigTypeKey("xds.service.discovery")
public class TemplateConfig implements PluginConfig {
    @ConfigFieldKey("upstreamServiceName")
    private String upstreamServiceName;

    @ConfigFieldKey("type")
    private String type;

    @ConfigFieldKey("enabled")
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUpstreamServiceName() {
        return upstreamServiceName;
    }

    public void setUpstreamServiceName(String upstreamServiceName) {
        this.upstreamServiceName = upstreamServiceName;
    }
}
