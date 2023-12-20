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
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPullConsumerController;
import com.huaweicloud.sermant.rocketmq.controller.RocketMqPushConsumerController;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * 消息队列禁止消费插件的动态配置监听器
 *
 * @author lilai
 * @since 2023-12-08
 */
public class MqConfigListener implements DynamicConfigListener {
    /**
     * 全局配置的Key
     */
    public static final String GLOBAL_CONFIG_KEY = "sermant.mq.consume.globalConfig";

    /**
     * 局部配置的key的前缀
     */
    public static final String LOCAL_CONFIG_KEY_PREFIX = "sermant.mq.consume.";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Yaml yaml;

    /**
     * 监听器构造方法
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
     * 处理创建或者更新配置的事件
     *
     * @param event 事件
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
     * 处理删除配置的事件
     *
     * @param event 事件
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
        KafkaConsumerController.getConsumerCache()
                .forEach(obj -> KafkaConsumerController.disableConsumption(obj,
                        ProhibitionConfigManager.getKafkaProhibitionTopics()));
        RocketMqPushConsumerController.getPushConsumerCache().entrySet()
                .forEach(obj -> RocketMqPushConsumerController.disablePushConsumption(obj.getValue(),
                        ProhibitionConfigManager.getRocketMqProhibitionTopics()));
        RocketMqPullConsumerController.getPullConsumerCache().entrySet()
                .forEach(obj -> RocketMqPullConsumerController.disablePullConsumption(obj.getValue(),
                        ProhibitionConfigManager.getRocketMqProhibitionTopics()));
    }

    /**
     * 处理启动时初始化配置的事件
     *
     * @param event 事件
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
