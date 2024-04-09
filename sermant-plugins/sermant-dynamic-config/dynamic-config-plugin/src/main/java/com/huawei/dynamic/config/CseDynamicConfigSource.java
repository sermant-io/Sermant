/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.dynamic.config;

import com.huaweicloud.sermant.core.plugin.subscribe.processor.OrderConfigEvent;
import com.huaweicloud.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import com.huaweicloud.sermant.core.utils.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * dynamic configuration in cse scenarios
 *
 * @author zhouss
 * @since 2022-04-22
 */
public class CseDynamicConfigSource extends DynamicConfigSource {
    private Map<String, Object> configSources = new HashMap<>();

    @Override
    protected boolean doAccept(DynamicConfigEvent event) {
        if (event instanceof OrderConfigEvent) {
            final Map<String, Object> result = new HashMap<>();
            MapUtils.resolveNestMap(result, ((OrderConfigEvent) event).getAllData(), null);
            this.configSources = result;
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getConfigNames() {
        return configSources.keySet();
    }

    @Override
    public Object getConfig(String key) {
        return configSources.get(key);
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public boolean isEnabled() {
        return configuration.isEnableCseAdapter();
    }
}
