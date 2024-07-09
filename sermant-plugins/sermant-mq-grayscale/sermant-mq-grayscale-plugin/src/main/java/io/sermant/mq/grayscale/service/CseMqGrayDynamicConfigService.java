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

package io.sermant.mq.grayscale.service;

import io.sermant.core.plugin.service.PluginService;
import io.sermant.core.plugin.subscribe.ConfigSubscriber;
import io.sermant.core.plugin.subscribe.CseGroupConfigSubscriber;

/**
 * grayscale dynamic config service
 *
 * @author chengyouling
 * @since 2024-05-27
 **/
public class CseMqGrayDynamicConfigService implements PluginService {
    @Override
    public void start() {
        CseMqGrayConfigListener listener = new CseMqGrayConfigListener();
        ConfigSubscriber subscriber = new CseGroupConfigSubscriber("default", listener, "SERMANT-MQ-GRAYSCALE");
        subscriber.subscribe();
    }
}
