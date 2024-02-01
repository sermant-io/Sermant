/*
 * Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.database.dynamicconfig;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionConfig;
import com.huaweicloud.sermant.database.config.DatabaseWriteProhibitionManager;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据层中间件禁写插件的动态配置监听器
 *
 * @author daizhenyu
 * @since 2024-01-26
 */
public class DatabaseConfigListener implements DynamicConfigListener {
    /**
     * 全局配置的Key
     */
    public static final String GLOBAL_CONFIG_KEY = "sermant.database.write.globalConfig";

    /**
     * 局部配置的key的前缀
     */
    public static final String LOCAL_CONFIG_KEY_PREFIX = "sermant.database.write.";

    private static final Logger LOGGER = LoggerFactory.getLogger();

    private final Yaml yaml;

    /**
     * 监听器构造方法
     */
    public DatabaseConfigListener() {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        this.yaml = new Yaml(representer);
    }

    @Override
    public void process(DynamicConfigEvent event) {
        try {
            if (event.getEventType() == DynamicConfigEventType.DELETE) {
                processDeleteEvent(event);
                return;
            }
            processInitOrCreateOrUpdateEvent(event);
        } catch (YAMLException e) {
            LOGGER.log(Level.SEVERE, "Fail to convert dynamic database-write-prohibition config, {0}", e.getMessage());
        }
    }

    /**
     * 处理初始化、创建或者更新配置的事件
     *
     * @param event 事件
     */
    private void processInitOrCreateOrUpdateEvent(DynamicConfigEvent event) {
        if (GLOBAL_CONFIG_KEY.equals(event.getKey())) {
            DatabaseWriteProhibitionManager
                    .updateGlobalConfig(yaml.loadAs(event.getContent(), DatabaseWriteProhibitionConfig.class));
        }
        if ((LOCAL_CONFIG_KEY_PREFIX + ConfigManager.getConfig(ServiceMeta.class).getService()).equals(
                event.getKey())) {
            DatabaseWriteProhibitionManager
                    .updateLocalConfig(yaml.loadAs(event.getContent(), DatabaseWriteProhibitionConfig.class));
        }
        if (event.getEventType() == DynamicConfigEventType.INIT) {
            LOGGER.log(Level.INFO, "Init database-write-prohibition config, current config: {0}",
                    DatabaseWriteProhibitionManager.printConfig());
            return;
        }
        LOGGER.log(Level.INFO, "Update database-write-prohibition config, current config: {0}",
                DatabaseWriteProhibitionManager.printConfig());
    }

    /**
     * 处理删除配置的事件
     *
     * @param event 事件
     */
    private void processDeleteEvent(DynamicConfigEvent event) {
        if (GLOBAL_CONFIG_KEY.equals(event.getKey())) {
            DatabaseWriteProhibitionManager.updateGlobalConfig(new DatabaseWriteProhibitionConfig());
        }
        if ((LOCAL_CONFIG_KEY_PREFIX + ConfigManager.getConfig(ServiceMeta.class).getService()).equals(
                event.getKey())) {
            DatabaseWriteProhibitionManager.updateLocalConfig(new DatabaseWriteProhibitionConfig());
        }
        LOGGER.log(Level.INFO, "Delete database-write-prohibition config, current config: {0}",
                DatabaseWriteProhibitionManager.printConfig());
    }
}
