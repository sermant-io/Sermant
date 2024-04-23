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

package io.sermant.implement.config;

import com.alibaba.fastjson.util.IOUtils;

import io.sermant.core.common.CommonConstant;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.common.BaseConfig;
import io.sermant.core.config.common.ConfigFieldKey;
import io.sermant.core.config.strategy.LoadConfigStrategy;
import io.sermant.core.config.utils.ConfigKeyUtil;
import io.sermant.core.config.utils.ConfigValueUtil;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loading strategy for yaml files
 * <p>yaml format transformations do not support {@link ConfigFieldKey} modifying field names for complex objects
 * involved in arrays, lists, and maps
 * <p>yaml format transformations do not support {@code ${}} conversion for strings in arrays, lists, and maps
 * <p>yaml format conversion uses input parameters only when the string is converted to {@code ${}}. It does not
 * support the use of input parameters to set field values directly
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class LoadYamlStrategy implements LoadConfigStrategy<Map> {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Map<Class<?>, Class<?>> BASE_TYPE_TRANSFER_MAP = new HashMap<Class<?>, Class<?>>() {
        {
            put(int.class, Integer.class);
            put(short.class, Short.class);
            put(long.class, Long.class);
            put(char.class, Character.class);
            put(byte.class, Byte.class);
            put(float.class, Float.class);
            put(double.class, Double.class);
            put(boolean.class, Boolean.class);
        }
    };

    /**
     * Yaml object
     */
    private final Yaml yaml;

    /**
     * argsMap
     */
    private Map<String, Object> argsMap;

    /**
     * Constructor
     */
    public LoadYamlStrategy() {
        Representer representer = new Representer(new DumperOptions());
        representer.getPropertyUtils().setSkipMissingProperties(true);
        this.yaml = new Yaml(representer);
    }

    @Override
    public boolean canLoad(File file) {
        final String fileName = file.getName();
        return fileName.endsWith(".yaml") || fileName.endsWith(".yml");
    }

    @Override
    public Map getConfigHolder(File config, Map<String, Object> bootstreapArgsMap) {
        this.argsMap = bootstreapArgsMap;
        return readConfig(config);
    }

    @Override
    public <R extends BaseConfig> R loadConfig(Map holder, R config) {
        final Class<R> cls = (Class<R>) config.getClass();
        final String typeKey = ConfigKeyUtil.getTypeKey(cls);
        final Object typeVal = holder.get(typeKey);
        if (!(typeVal instanceof Map)) {
            return config;
        }

        Map configMap = (Map) typeVal;
        for (Field field : cls.getDeclaredFields()) {
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (!configMap.containsKey(field.getName())) {
                configMap.put(field.getName(), null);
            }
        }
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cls.getClassLoader());
            return (R) yaml.loadAs(yaml.dump(fixEntry(configMap, cls)), cls);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    /**
     * Read file contents
     *
     * @param config Configuration file
     * @return Configuration information
     */
    private Map<?, ?> readConfig(File config) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(config),
                    CommonConstant.DEFAULT_CHARSET));
            return yaml.loadAs(reader, Map.class);
        } catch (IOException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Missing config file [%s], please check.", config));
        } finally {
            IOUtils.close(reader);
        }
        return Collections.emptyMap();
    }

    /**
     * Modify key, if the filed is modified by {@link ConfigFieldKey}, converts {@link ConfigFieldKey#value()} to the
     * filed value
     *
     * @param typeMap Class configuration information
     * @param cls Class
     * @return Field map of the class
     */
    private Map fixEntry(Map typeMap, Class<?> cls) {
        if (cls == Object.class || Map.class.isAssignableFrom(cls)) {
            return typeMap;
        }
        Map fixedTypeMap = fixEntry(typeMap, cls.getSuperclass());
        for (Field field : cls.getDeclaredFields()) {
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            final ConfigFieldKey configFieldKey = field.getAnnotation(ConfigFieldKey.class);
            final String fieldKey = field.getName();
            final Object subTypeVal;
            if (configFieldKey != null && fixedTypeMap.get(configFieldKey.value()) != null) {
                subTypeVal = fixedTypeMap.remove(configFieldKey.value());
            } else {
                subTypeVal = fixedTypeMap.get(fieldKey);
            }
            final Object fixedVal;
            if (subTypeVal instanceof Map) {
                fixedVal = fixEntry((Map) subTypeVal, field.getType());
                if (configFieldKey != null) {
                    fixedTypeMap.put(fieldKey, fixedVal);
                }
            } else {
                fixedVal = fixValStr(field, formatConfigKey(fieldKey, cls), fixedTypeMap, subTypeVal);
                if (fixedVal == null) {
                    fixedTypeMap.remove(fieldKey);
                } else {
                    fixedTypeMap.put(fieldKey, fixedVal);
                }
            }
        }
        return fixedTypeMap;
    }

    private String formatConfigKey(String fieldKey, Class<?> cls) {
        return String.format(Locale.ENGLISH, "%s.%s", ConfigKeyUtil.getTypeKey(cls), fieldKey);
    }

    /**
     * Fix values in the form of "${}"
     *
     * @param field field
     * @param configKey config key
     * @param typeMap parent map
     * @param subTypeVal current value
     * @return fixed value
     */
    private Object fixValStr(Field field, String configKey, Map typeMap, Object subTypeVal) {
        final ConfigValueUtil.FixedValueProvider provider = new ConfigValueUtil.FixedValueProvider() {
            @Override
            public String getFixedValue(String key) {
                final Object fixedVal = typeMap.get(key);
                if (fixedVal instanceof String || fixedVal instanceof Integer) {
                    return fixedVal.toString();
                }
                return null;
            }
        };

        Object fixedVal = null;
        try {
            if (subTypeVal instanceof String) {
                fixedVal = ConfigValueUtil.fixValue(configKey, (String) subTypeVal, argsMap, provider);
            } else if (subTypeVal == null) {
                String fixedStrValue = ConfigValueUtil.fixValue(configKey, null, argsMap, provider);
                if (fixedStrValue == null) {
                    fixedVal = null;
                } else {
                    Class fieldClass = BASE_TYPE_TRANSFER_MAP.getOrDefault(field.getType(), field.getType());
                    fixedVal = yaml.loadAs(fixedStrValue, fieldClass);
                }
            } else {
                Class fieldClass = subTypeVal.getClass();
                fixedVal = yaml.loadAs(ConfigValueUtil.fixValue(configKey, yaml.dump(subTypeVal), argsMap, provider),
                        fieldClass);
            }
        } catch (ConstructorException exception) {
            LOGGER.severe(String.format(Locale.ENGLISH, "Error occurs while parsing configKey: %s", configKey));
        }

        return fixedVal;
    }
}
