/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.config.utils;

import io.sermant.core.config.common.ConfigFieldKey;
import io.sermant.core.config.common.ConfigTypeKey;

import java.lang.reflect.Field;

/**
 * Tool for handling key of configuration system
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-16
 */
public class ConfigKeyUtil {
    private ConfigKeyUtil() {
    }

    /**
     * Gets the key of the configuration object
     * <p>If the configuration object is modified by {@link ConfigTypeKey}, take its value
     * <p>If not modified by {@link ConfigTypeKey}, take the fully qualified name of the class
     *
     * @param cls Configuration object class
     * @return prefix string
     */
    public static String getTypeKey(Class<?> cls) {
        final ConfigTypeKey configTypeKey = cls.getAnnotation(ConfigTypeKey.class);
        if (configTypeKey == null) {
            return cls.getName();
        } else {
            return configTypeKey.value();
        }
    }

    /**
     * Obtain the configuration information key
     * <p>Get the configuration information key corresponding to the member attribute with the {@link ConfigFieldKey}
     * annotation
     * <p>When no annotation exists, take the field name directly
     *
     * @param field filed
     * @return Configuration information key
     */
    public static String getFieldKey(Field field) {
        final ConfigFieldKey configFieldKey = field.getAnnotation(ConfigFieldKey.class);
        if (configFieldKey == null) {
            return field.getName();
        } else {
            return configFieldKey.value();
        }
    }

    /**
     * Type key with ClassLoader
     *
     * @param typeKey type key
     * @param classLoader ClassLoader
     * @return Type key with ClassLoader
     */
    public static String getTypeKeyWithClassloader(String typeKey, ClassLoader classLoader) {
        return typeKey + "@" + Integer.toHexString(classLoader.hashCode());
    }
}
