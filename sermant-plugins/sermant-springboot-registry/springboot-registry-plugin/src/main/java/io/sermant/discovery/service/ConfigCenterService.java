/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.discovery.service;

import io.sermant.core.plugin.service.PluginService;
import io.sermant.core.plugin.subscribe.ConfigSubscriber;
import io.sermant.core.plugin.subscribe.CseGroupConfigSubscriber;
import io.sermant.core.utils.StringUtils;
import io.sermant.discovery.config.EffectStategyDynamicConfigListener;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The plugin takes effect on the dynamic configuration service
 *
 * @author chengyouling
 * @since 2022-09-26
 */
public class ConfigCenterService implements PluginService {
    private final AtomicBoolean isRun = new AtomicBoolean();

    /**
     * Instantiate dynamic configuration listeners
     *
     * @param serviceName Host service name
     */
    public void init(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            return;
        }
        if (isRun.compareAndSet(false, true)) {
            ConfigSubscriber configSubscriber = new CseGroupConfigSubscriber(
                    serviceName, new EffectStategyDynamicConfigListener(), "serment-springboot-registry");
            configSubscriber.subscribe();
        }
    }
}
