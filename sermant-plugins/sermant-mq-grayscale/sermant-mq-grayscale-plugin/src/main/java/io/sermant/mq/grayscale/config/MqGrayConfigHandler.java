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

package io.sermant.mq.grayscale.config;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.core.utils.StringUtils;
import io.sermant.mq.grayscale.utils.MqGrayscaleConfigUtils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * grayscale dynamic config handler
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class MqGrayConfigHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * the key is configured globally
     */
    private static final String GRAY_CONFIG_KEY = "grayscale.mq.config";

    private final Yaml yaml;

    /**
     * construction
     */
    public MqGrayConfigHandler() {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        this.yaml = new Yaml(representer);
    }

    /**
     * processing configuration
     *
     * @param event event
     */
    public void handle(DynamicConfigEvent event) {
        if (!GRAY_CONFIG_KEY.equals(event.getKey())) {
            return;
        }
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            MqGrayscaleConfigUtils.resetGrayscaleConfig();
            return;
        }
        if (!StringUtils.isEmpty(event.getContent())) {
            LOGGER.info(String.format(Locale.ROOT, "mqGrayscale [%s] dynamicConfig context: %s",
                    event.getGroup(), event.getContent()));
            MqGrayscaleConfig config = yaml.loadAs(event.getContent(), MqGrayscaleConfig.class);
            if (config != null) {
                MqGrayscaleConfigUtils.setGrayscaleConfig(config, event.getEventType());
            }
        }
    }
}
