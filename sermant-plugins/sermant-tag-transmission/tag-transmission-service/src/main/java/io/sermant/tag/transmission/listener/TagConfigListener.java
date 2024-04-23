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

package io.sermant.tag.transmission.listener;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigListener;
import io.sermant.tag.transmission.config.TagTransmissionConfig;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * traffic tag dynamically configures the listener
 *
 * @author lilai
 * @since 2023-07-20
 */
public class TagConfigListener implements DynamicConfigListener {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private TagTransmissionConfig tagTransmissionConfig;

    private final Yaml yaml;

    /**
     * construction method
     */
    public TagConfigListener() {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        this.yaml = new Yaml(representer);
        this.tagTransmissionConfig = PluginConfigManager.getPluginConfig(TagTransmissionConfig.class);
    }

    @Override
    public void process(DynamicConfigEvent event) {
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            tagTransmissionConfig.setEnabled(false);
            return;
        }
        try {
            updateConfig(event);
        } catch (YAMLException e) {
            LOGGER.severe(String.format(Locale.ROOT, "Fail to convert dynamic tag config, %s", e.getMessage()));
        }
    }

    private void updateConfig(DynamicConfigEvent event) {
        TagTransmissionConfig dynamicConfig = yaml.loadAs(event.getContent(), TagTransmissionConfig.class);
        if (dynamicConfig == null) {
            return;
        }
        tagTransmissionConfig.setEnabled(dynamicConfig.isEnabled());
        tagTransmissionConfig.setMatchRule(dynamicConfig.getMatchRule());
        LOGGER.info(String.format(Locale.ROOT, "Update tagTransmissionConfig, %s",
                tagTransmissionConfig.toString()));
    }
}
