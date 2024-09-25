/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.backend.entity.config;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * configuration information
 *
 * @author zhp
 * @since 2024-05-16
 */
@Getter
@Setter
public class ConfigInfo {
    /**
     * plugin type, the plugin to which the configuration item belongs
     */
    private String pluginType;

    /**
     * The namespace to which the configuration item belongs, only used by the nacos configuration center
     */
    private String namespace;

    /**
     * Service name, when not empty, the configuration item only takes effect on microservices
     * with the same service name
     */
    private String serviceName;

    /**
     * Environment name, when not empty, the configuration item only takes effect for microservices in that environment
     */
    private String environment;

    /**
     * Application name, when not empty, the current configuration item only applies to microservices
     * under that application
     */
    private String appName;

    /**
     * Zone name, when not empty, the configuration item only applies to microservices in that zone
     */
    private String zone;

    /**
     * Grouping name for configuration items
     */
    private String group;

    /**
     * The key of the configuration item
     */
    private String key;

    /**
     * Configuration Content
     */
    private String content;

    /**
     * Group generation rules
     */
    private String groupRule;

    /**
     * Key generation rules
     */
    private String keyRule;

    /**
     * Indicator for exact matching
     */
    private boolean exactMatchFlag;

    /**
     * Constructor
     *
     * @param key The key of the configuration item
     * @param group Grouping name for configuration items
     * @param keyRule Key generation rules
     * @param groupRule Group generation rules
     * @param namespace The namespace to which the configuration item belongs
     */
    public ConfigInfo(String key, String group, String keyRule, String groupRule, String namespace) {
        this.key = key;
        this.group = group;
        this.groupRule = groupRule;
        this.keyRule = keyRule;
        this.namespace = namespace;
    }

    /**
     * Constructor
     *
     * @param keyRule Key generation rules
     * @param groupRule Group generation rules
     * @param pluginType plugin type, the plugin to which the configuration item belongs
     * @param exactMatchFlag Indicator for exact matching
     * @param namespace The namespace to which the configuration item belongs
     */
    public ConfigInfo(String keyRule, String groupRule, String pluginType, boolean exactMatchFlag, String namespace) {
        this.keyRule = keyRule;
        this.groupRule = groupRule;
        this.pluginType = pluginType;
        this.exactMatchFlag = exactMatchFlag;
        this.namespace = namespace;
    }

    /**
     * Constructor
     */
    public ConfigInfo() {
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConfigInfo configInfo = (ConfigInfo) obj;
        return StringUtils.equals(this.getKey(), configInfo.getKey())
                && StringUtils.equals(this.getGroup(), configInfo.getGroup());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getKey(), this.getGroup());
    }
}
