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

package io.sermant.core.service.metric.entity;

import io.sermant.core.common.BootArgsIndexer;
import io.sermant.core.utils.NetworkUtils;
import io.sermant.core.utils.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Enum class to define and manage metric tags with their keys and value supply strategies.
 *
 * @author zwmagic
 * @since 2024-12-18
 */
public enum MetricCommonTagEnum {
    /**
     * Define an AGENT tag with key "agent" and a constant value "sermant".
     */
    AGENT("agent", true, () -> "sermant"),
    /**
     * Define an AGENT_APP_NAME tag with key "agent.app.name" and a value supplied by appName.
     */
    AGENT_APP_NAME("agent.app.name", true, BootArgsIndexer::getAppName),
    /**
     * Define an AGENT_IP tag with key "agent.ip" and a value supplied by machine ip.
     */
    AGENT_IP("agent.ip", true, NetworkUtils::getMachineIp),
    /**
     * Define an SCOPE tag with key "scope" and a value supplied by core or plugin.
     */
    SCOPE("scope", true, () -> "undefined"),
    /**
     * Define an AGENT_SERVICE_NAME tag with key "agent.service.name" and a value supplied by serviceName
     */
    AGENT_SERVICE_NAME("agent.service.name", false, BootArgsIndexer::getServiceName),
    /**
     * Define an AGENT_APP_TYPE tag with key "agent.app.type" and a value supplied by appType
     */
    AGENT_APP_TYPE("agent.app.type", false, BootArgsIndexer::getAppType),
    /**
     * Define an AGENT_ARTIFACT tag with key "agent.artifact" and a value supplied by artifact
     */
    AGENT_ARTIFACT("agent.artifact", false, BootArgsIndexer::getArtifact),
    /**
     * Define an AGENT_VERSION tag with key "agent.version" and a value supplied by agent version
     */
    AGENT_VERSION("agent.version", false, BootArgsIndexer::getCoreVersion);

    private final String key;

    private final boolean defaultEnable;

    private final Supplier<String> valueSupplier;

    /**
     * Constructor to initialize MetricTagKeyEnum enum members.
     *
     * @param key The key of the tag
     * @param defaultEnable default enable for the tag
     * @param valueSupplier The strategy to provide the tag's value
     */
    MetricCommonTagEnum(String key, boolean defaultEnable, Supplier<String> valueSupplier) {
        this.key = key;
        this.defaultEnable = defaultEnable;
        this.valueSupplier = valueSupplier;
    }

    /**
     * Get the key of the tag.
     *
     * @return The key of the tag
     */
    public String getKey() {
        return key;
    }

    /**
     * Check if the tag is default enabled.
     *
     * @return true if the tag is default enabled, false otherwise
     */
    public boolean isDefaultEnable() {
        return defaultEnable;
    }

    /**
     * Get the strategy to provide the tag's value.
     *
     * @return The strategy to provide the tag's value
     */
    public Supplier<String> getValueSupplier() {
        return valueSupplier;
    }

    /**
     * Get a set of all default-enabled tag keys.
     *
     * @return A set containing all default-enabled tag keys
     */
    public static Set<String> getDefaultKeys() {
        return Arrays.stream(MetricCommonTagEnum.values()).filter(MetricCommonTagEnum::isDefaultEnable)
                .map(MetricCommonTagEnum::getKey).collect(Collectors.toSet());
    }

    /**
     * Get the value of the tag corresponding to the given key. If the provided key is empty or does not correspond to
     * any tag, return an empty string.
     *
     * @param key The key of the tag
     * @return The value of the tag, or an empty string if no matching tag exists
     */
    public static String of(String key) {
        if (StringUtils.isEmpty(key)) {
            return StringUtils.EMPTY;
        }
        for (MetricCommonTagEnum tagKeyEnum : MetricCommonTagEnum.values()) {
            if (tagKeyEnum.getKey().equals(key)) {
                return tagKeyEnum.getValueSupplier().get();
            }
        }
        return StringUtils.EMPTY;
    }
}
