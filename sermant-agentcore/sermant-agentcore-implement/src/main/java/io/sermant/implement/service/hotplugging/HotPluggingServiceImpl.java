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

package io.sermant.implement.service.hotplugging;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.dynamicconfig.DynamicConfigService;
import io.sermant.core.service.hotplugging.HotPluggingService;
import io.sermant.implement.service.hotplugging.listener.HotPluggingListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hot plugging service implementation
 *
 * @author zhp
 * @since 2024-08-01
 */
public class HotPluggingServiceImpl implements HotPluggingService {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final String DYNAMIC_CONFIG_GROUP = "sermant-hot-plugging";

    private static final String DYNAMIC_CONFIG_KEY = "config";

    @Override
    public void start() {
        DynamicConfigService dynamicConfigService;
        try {
            dynamicConfigService = ServiceManager.getService(DynamicConfigService.class);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "The dynamic configuration service does not start and the hotplugging "
                    + "service cannot start normally.");
            return;
        }
        dynamicConfigService.addConfigListener(DYNAMIC_CONFIG_KEY, DYNAMIC_CONFIG_GROUP,
                new HotPluggingListener(), true);
        LOGGER.log(Level.INFO, "Success to subscribe {0}/{1}", new Object[]{DYNAMIC_CONFIG_GROUP, DYNAMIC_CONFIG_KEY});
    }

    @Override
    public void stop() {
        DynamicConfigService dynamicConfigService = ServiceManager.getService(DynamicConfigService.class);
        dynamicConfigService.doRemoveConfigListener(DYNAMIC_CONFIG_KEY, DYNAMIC_CONFIG_GROUP);
    }
}
