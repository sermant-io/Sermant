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

package com.huaweicloud.sermant.tag.transmission.config;

import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 流量标签透传插件配置
 *
 * @author lilai
 * @since 2023-07-17
 */
@ConfigTypeKey("tag.transmission.plugin")
public class TagTransmissionConfig implements PluginConfig {
    /**
     * 是否开启适配
     */
    private boolean enabled;

    /**
     * 需要透传的标签的key
     */
    private List<String> tagKeys = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getTagKeys() {
        return tagKeys;
    }

    public void setTagKeys(List<String> tagKeys) {
        this.tagKeys = tagKeys;
    }
}
