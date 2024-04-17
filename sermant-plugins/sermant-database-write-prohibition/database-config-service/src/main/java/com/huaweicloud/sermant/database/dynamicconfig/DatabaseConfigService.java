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
import com.huaweicloud.sermant.core.plugin.service.PluginService;
import com.huaweicloud.sermant.core.plugin.subscribe.CommonGroupConfigSubscriber;
import com.huaweicloud.sermant.core.plugin.subscribe.ConfigSubscriber;

import java.util.logging.Logger;

/**
 * Data layer middleware writing-prohibition dynamic configuration services
 *
 * @author daizhenyu
 * @since 2024-01-27
 */
public class DatabaseConfigService implements PluginService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    @Override
    public void start() {
        ConfigSubscriber subscriber = new CommonGroupConfigSubscriber(
                ConfigManager.getConfig(ServiceMeta.class).getService(),
                new DatabaseConfigListener(), "database-write-prohibition");
        subscriber.subscribe();
        LOGGER.info("Success to subscribe database-write-prohibition config");
    }
}
