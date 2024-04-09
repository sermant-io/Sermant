/*
 * Copyright (C) 2023-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.tag.transmission.service;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.service.ServiceManager;
import com.huaweicloud.sermant.core.service.dynamicconfig.DynamicConfigService;
import com.huaweicloud.sermant.tag.transmission.listener.TagConfigListener;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Traffic transparent dynamic configuration of the listening service
 *
 * @author lilai
 * @since 2023-07-20
 */
public class TagConfigService implements PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String DYNAMIC_CONFIG_KEY = "tag-config";

    private static final String DYNAMIC_CONFIG_GROUP = "sermant/tag-transmission-plugin";

    @Override
    public void start() {
        DynamicConfigService dynamicConfigService = ServiceManager.getService(DynamicConfigService.class);
        dynamicConfigService.addConfigListener(DYNAMIC_CONFIG_KEY, DYNAMIC_CONFIG_GROUP,
                new TagConfigListener(), true);
        LOGGER.info(String.format(Locale.ROOT,
                "Success to subscribe %s/%s", DYNAMIC_CONFIG_GROUP, DYNAMIC_CONFIG_KEY));
    }
}
