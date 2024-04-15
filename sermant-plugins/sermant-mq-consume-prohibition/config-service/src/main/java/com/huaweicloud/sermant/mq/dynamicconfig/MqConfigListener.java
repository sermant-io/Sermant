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

package com.huaweicloud.sermant.mq.dynamicconfig;

import com.huaweicloud.sermant.config.ProhibitionConfig;
import com.huaweicloud.sermant.config.ProhibitionConfigManager;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.kafka.controller.KafkaConsumerController;
import com.huaweicloud.sermant.rocketmq.cache.RocketMqConsumerCache;
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPullConsumerController;
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPushConsumerController;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Dynamic configuration listener for message queue prohibited consumption plugin
 *
 * @author lilai
 * @since 2023-12-08
 */
public class MqConfigListener implements DynamicConfigListener {
    /**
     * Global Configuration Key
     */
    public static final String GLOBAL_CONFIG_KEY = "sermant.mq.consume.globalConfig";

    /**
     * Prefix for local configuration key
     */
    public static final String LOCAL_CONFIG_KEY_PREFIX = "sermant.mq.consume.";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Yaml yaml;

    /**
     * The Construction Method of Listener
     */
    public MqConfigListener() {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        this.yaml = new Yaml(representer);
    }

    @Override
    public void process(DynamicConfigEvent event) {
        try {
            if (event.getEventType() == DynamicConfigEventType.INIT) {
                processInitEvent(event);
                return;
            }

            if (event.getEventType() == DynamicConfigEventType.DELETE) {
                processDeleteEvent(event);
                return;
            }
            processCreateOrUpdateEvent(event);
        } catch (YAMLException e) {
            LOGGER.severe(String.format(Locale.ROOT, "Fail to convert dynamic mq-consume-prohibition config, %s",
                    e.getMessage()));
        }
    }

    /**
     * Handling events for creating or updating configurations
     *
     * @param event Dynamic configuration event
     */
    private void processCreateOrUpdateEvent(DynamicConfigEvent event) {
        if (GLOBAL_CONFIG_KEY.equals(event.getKey())) {
            ProhibitionConfigManager.updateGlobalConfig(yaml.loadAs(event.getContent(), ProhibitionConfig.class));
            executeProhibition();
        }
        if ((LOCAL_CONFIG_KEY_PREFIX + ConfigManager.getConfig(ServiceMeta.class).getService()).equals(
                event.getKey())) {
            ProhibitionConfigManager.updateLocalConfig(yaml.loadAs(event.getContent(), ProhibitionConfig.class));
            executeProhibition();
        }
        LOGGER.info(String.format(Locale.ROOT, "Update mq-consume-prohibition config, current config: %s",
                ProhibitionConfigManager.printConfig()));
    }

    /**
     * Handling events for deleting configurations
     *
     * @param event Dynamic configuration event
     */
    private void processDeleteEvent(DynamicConfigEvent event) {
        if (GLOBAL_CONFIG_KEY.equals(event.getKey())) {
            ProhibitionConfigManager.updateGlobalConfig(new ProhibitionConfig());
            executeProhibition();
        }
        if ((LOCAL_CONFIG_KEY_PREFIX + ConfigManager.getConfig(ServiceMeta.class).getService()).equals(
                event.getKey())) {
            ProhibitionConfigManager.updateLocalConfig(new ProhibitionConfig());
            executeProhibition();
        }
        LOGGER.info(String.format(Locale.ROOT, "Delete mq-consume-prohibition config, current config: %s",
                ProhibitionConfigManager.printConfig()));
    }

    private void executeProhibition() {
        // KafkaConsumer标记配置已更新
        KafkaConsumerController.getKafkaConsumerCache().values()
                .forEach(obj -> obj.getIsConfigChanged().set(true));
        RocketMqConsumerCache.PUSH_CONSUMERS_CACHE.entrySet()
                .forEach(obj -> RocketMqPushConsumerController.disablePushConsumption(obj.getValue(),
                        ProhibitionConfigManager.getRocketMqProhibitionTopics()));
        RocketMqConsumerCache.PULL_CONSUMERS_CACHE.entrySet()
                .forEach(obj -> RocketMqPullConsumerController.disablePullConsumption(obj.getValue(),
                        ProhibitionConfigManager.getRocketMqProhibitionTopics()));
    }

    /**
     * Handling events for initializing configuration at startup
     *
     * @param event Dynamic configuration event
     */
    private void processInitEvent(DynamicConfigEvent event) {
        if (GLOBAL_CONFIG_KEY.equals(event.getKey())) {
            ProhibitionConfigManager.updateGlobalConfig(
                    yaml.loadAs(event.getContent(), ProhibitionConfig.class));
        }
        if ((LOCAL_CONFIG_KEY_PREFIX + ConfigManager.getConfig(ServiceMeta.class).getService()).equals(
                event.getKey())) {
            ProhibitionConfigManager.updateLocalConfig(
                    yaml.loadAs(event.getContent(), ProhibitionConfig.class));
        }
        LOGGER.info(String.format(Locale.ROOT, "Init mq-consume-prohibition config, current config: %s",
                ProhibitionConfigManager.printConfig()));
    }
}
