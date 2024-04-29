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

package io.sermant.mq.prohibition.controller.config;

import java.util.HashSet;
import java.util.Set;

/**
 * Message queue prohibition consumption configuration management class
 *
 * @author lilai
 * @since 2023-12-07
 */
public class ProhibitionConfigManager {
    private static ProhibitionConfig globalConfig = new ProhibitionConfig();

    private static ProhibitionConfig localConfig = new ProhibitionConfig();

    private ProhibitionConfigManager() {
    }

    /**
     * Obtain the collection of topics that you want to prohibit consumption by Kafka
     *
     * @return The collection of topics that Kafka wants to prohibit from consuming
     */
    public static Set<String> getKafkaProhibitionTopics() {
        if (globalConfig.isEnableKafkaProhibition()) {
            return globalConfig.getKafkaTopics();
        }
        if (localConfig.isEnableKafkaProhibition()) {
            return localConfig.getKafkaTopics();
        }
        return new HashSet<>();
    }

    /**
     * Obtain the collection of topics that rocketmq is to prohibit consumption
     *
     * @return The collection of topics that rocketmq wants to prohibit from consuming
     */
    public static Set<String> getRocketMqProhibitionTopics() {
        if (globalConfig.isEnableRocketMqProhibition()) {
            return globalConfig.getRocketMqTopics();
        }
        if (localConfig.isEnableRocketMqProhibition()) {
            return localConfig.getRocketMqTopics();
        }
        return new HashSet<>();
    }

    /**
     * Get the global configuration
     *
     * @return Global configuration
     */
    public static ProhibitionConfig getGlobalConfig() {
        return globalConfig;
    }

    /**
     * Get the local configuration
     *
     * @return Local configuration
     */
    public static ProhibitionConfig getLocalConfig() {
        return localConfig;
    }

    /**
     * Update the global configuration
     *
     * @param config prohibition consumption configuration
     */
    public static void updateGlobalConfig(ProhibitionConfig config) {
        if (config == null) {
            globalConfig = new ProhibitionConfig();
            return;
        }
        globalConfig = config;
    }

    /**
     * Update local configurations
     *
     * @param config prohibition consumption configuration
     */
    public static void updateLocalConfig(ProhibitionConfig config) {
        if (config == null) {
            localConfig = new ProhibitionConfig();
            return;
        }
        localConfig = config;
    }

    /**
     * Print the configuration information
     *
     * @return Configuration information
     */
    public static String printConfig() {
        return "Global ProhibitionConfig: " + globalConfig.toString() + "; Local ProhibitionConfig: "
                + localConfig.toString();
    }
}
