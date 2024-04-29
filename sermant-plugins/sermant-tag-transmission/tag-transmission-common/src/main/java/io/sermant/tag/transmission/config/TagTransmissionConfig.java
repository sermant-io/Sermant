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

package io.sermant.tag.transmission.config;

import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;
import io.sermant.core.utils.MapUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Traffic tag transparent transmission configuration
 *
 * @author lilai
 * @since 2023-07-17
 */
@ConfigTypeKey("tag.transmission.config")
public class TagTransmissionConfig implements PluginConfig {
    /**
     * whether to enable
     */
    private boolean enabled;

    /**
     * Rules for the key of the tag that needs to be transmitted
     */
    private Map<String, List<String>> matchRule = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Whether the traffic tag transparent transmission rule is effect
     *
     * @return boolean
     */
    public boolean isEffect() {
        return enabled && !MapUtils.isEmpty(matchRule);
    }

    @Override
    public String toString() {
        return "TagTransmissionConfig{"
                + "enabled=" + enabled
                + ", matchRule=" + matchRule
                + '}';
    }

    public Map<String, List<String>> getMatchRule() {
        return matchRule;
    }

    public void setMatchRule(Map<String, List<String>> matchRule) {
        this.matchRule = matchRule;
    }
}
