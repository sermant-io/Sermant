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

package com.huaweicloud.sermant.config;

import java.util.HashSet;
import java.util.Set;

/**
 * 消息队列禁止消费配置管理类
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
     * 获取kafka要禁止消费的Topic集合
     *
     * @return kafka要禁止消费的Topic集合
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
     * 获取rocketmq要禁止消费的Topic集合
     *
     * @return rocketmq要禁止消费的Topic集合
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
     * 获取全局配置
     *
     * @return 全局配置
     */
    public static ProhibitionConfig getGlobalConfig() {
        return globalConfig;
    }

    /**
     * 获取局部配置
     *
     * @return 局部配置
     */
    public ProhibitionConfig getLocalConfig() {
        return localConfig;
    }

    /**
     * 更新全局配置
     *
     * @param config 禁止消费配置
     */
    public static void updateGlobalConfig(ProhibitionConfig config) {
        if (config == null) {
            globalConfig = new ProhibitionConfig();
            return;
        }
        globalConfig = config;
    }

    /**
     * 更新局部配置
     *
     * @param config 禁止消费配置
     */
    public static void updateLocalConfig(ProhibitionConfig config) {
        if (config == null) {
            localConfig = new ProhibitionConfig();
            return;
        }
        localConfig = config;
    }

    /**
     * 打印配置信息
     *
     * @return 配置信息
     */
    public static String printConfig() {
        return "Global ProhibitionConfig: " + globalConfig.toString() + "; Local ProhibitionConfig: "
                + localConfig.toString();
    }
}
