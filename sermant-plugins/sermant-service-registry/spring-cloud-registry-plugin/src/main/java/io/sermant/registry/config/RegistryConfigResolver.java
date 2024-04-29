/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.sermant.registry.config;

import io.sermant.core.operation.OperationManager;
import io.sermant.core.operation.converter.api.YamlConverter;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEvent;
import io.sermant.core.service.dynamicconfig.common.DynamicConfigEventType;
import io.sermant.core.utils.MapUtils;
import io.sermant.core.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Configuration analysis related to Spring registration
 *
 * @author zhouss
 * @since 2022-05-24
 */
public abstract class RegistryConfigResolver {
    private final YamlConverter yamlConverter = OperationManager.getOperation(YamlConverter.class);

    /**
     * Updated the configuration of elegant online and offline lines
     *
     * @param event Notification of events
     */
    public void updateConfig(DynamicConfigEvent event) {
        if (!isTargetConfig(event)) {
            return;
        }
        if (updateWithDefaultMode(event)) {
            afterUpdateConfig();
        }
    }

    private boolean updateConfig(Map<String, Object> dic, DynamicConfigEventType eventType) {
        final Object defaultGraceConfig = getDefaultConfig();
        final Object originGraceConfig = getOriginConfig();
        final Field[] declaredFields = originGraceConfig.getClass().getDeclaredFields();
        boolean isUpdated = false;
        for (Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            final String name = field.getName();
            final Object configValue = dic.get(getConfigPrefix() + name);
            if (configValue == null || eventType == DynamicConfigEventType.DELETE) {
                // The default value will be changed to the value read from the configuration and environment variables
                // for the first time
                final Optional<Object> fieldValue = ReflectUtils.getFieldValue(defaultGraceConfig, name);
                fieldValue.ifPresent(value -> ReflectUtils.setFieldValue(originGraceConfig, name, value));
                isUpdated = true;
                continue;
            }
            isUpdated = true;

            // If the configuration center has this value, it will be overwritten
            ReflectUtils.setFieldValue(originGraceConfig, name, configValue);
        }
        return isUpdated;
    }

    private boolean updateWithDefaultMode(DynamicConfigEvent event) {
        if (event.getEventType() == DynamicConfigEventType.DELETE) {
            return updateConfig(Collections.emptyMap(), event.getEventType());
        } else {
            final Optional<Map<String, Object>> convert = yamlConverter.convert(event.getContent(), Map.class);
            if (convert.isPresent()) {
                final Map<String, Object> data = convert.get();
                final HashMap<String, Object> result = new HashMap<>(data.size());
                MapUtils.resolveNestMap(result, data, null);
                return this.updateConfig(result, event.getEventType());
            }
            return false;
        }
    }

    /**
     * Obtain the configuration prefix
     *
     * @return Configure the prefix
     */
    protected abstract String getConfigPrefix();

    /**
     * Gets the default configuration class
     *
     * @return The default configuration class
     */
    protected abstract Object getDefaultConfig();

    /**
     * Get the original configuration class, the configuration obtained through the PluginConfigManager
     *
     * @return Original configuration class
     */
    protected abstract Object getOriginConfig();

    /**
     * Whether it is configured for the target
     *
     * @param event Configure listening events
     * @return If configured for the target, true is returned
     */
    protected abstract boolean isTargetConfig(DynamicConfigEvent event);

    /**
     * What to do after the configuration is updated
     */
    protected abstract void afterUpdateConfig();
}
