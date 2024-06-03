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

import io.sermant.backend.handler.config.DatabaseWriteProhibitionPluginHandler;
import io.sermant.backend.handler.config.FlowControlPluginHandler;
import io.sermant.backend.handler.config.LoadbalancerPluginHandler;
import io.sermant.backend.handler.config.MqConsumeProhibitionPluginHandler;
import io.sermant.backend.handler.config.OtherPluginHandler;
import io.sermant.backend.handler.config.PluginConfigHandler;
import io.sermant.backend.handler.config.RemovalPluginHandler;
import io.sermant.backend.handler.config.RouterPluginHandler;
import io.sermant.backend.handler.config.ServiceRegistryPluginHandler;
import io.sermant.backend.handler.config.SpringBootRegistryPluginHandler;
import io.sermant.backend.handler.config.TagTransmissionPluginHandler;
import lombok.Getter;

import java.util.Optional;

/**
 * Plugin type
 *
 * @author zhp
 * @since 2024-05-16
 */
@Getter
public enum PluginType {
    /**
     * Label routing plugin
     */
    ROUTER("router", new RouterPluginHandler()),

    /**
     * Springboot registration plugin
     */
    SPRINGBOOT_REGISTRY("springboot-registry", new SpringBootRegistryPluginHandler()),

    /**
     * Register migration plugin
     */
    SERVICE_REGISTRY("service-registry", new ServiceRegistryPluginHandler()),

    /**
     * flowcontrol plugin
     */
    FLOW_CONTROL("flowcontrol", new FlowControlPluginHandler()),

    /**
     * Outlier instance removal plugin
     */
    REMOVAL("removal", new RemovalPluginHandler()),

    /**
     * Load balancing plugin
     */
    LOADBALANCER("loadbalancer", new LoadbalancerPluginHandler()),

    /**
     * Traffic tag transparency plugin
     */
    TAG_TRANSMISSION("tag-transmission", new TagTransmissionPluginHandler()),

    /**
     * Message queue prohibited consumption plugin
     */
    MQ_CONSUME_PROHIBITION("mq-consume-prohibition", new MqConsumeProhibitionPluginHandler()),

    /**
     * Database write prohibited plugin
     */
    DATABASE_WRITE_PROHIBITION("database-write-prohibition", new DatabaseWriteProhibitionPluginHandler()),

    /**
     * other plugin
     */
    OTHER("other", new OtherPluginHandler());

    private final String pluginName;

    private final PluginConfigHandler handler;

    PluginType(String pluginName, PluginConfigHandler handler) {
        this.pluginName = pluginName;
        this.handler = handler;
    }

    /**
     * Obtain plugin type based on plugin name
     *
     * @param pluginName plugin name
     * @return plugin type
     */
    public static Optional<PluginType> getPluginType(String pluginName) {
        for (PluginType pluginType : PluginType.values()) {
            if (pluginType.pluginName.equals(pluginName)) {
                return Optional.of(pluginType);
            }
        }
        return Optional.empty();
    }
}
