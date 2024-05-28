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

import io.sermant.core.common.CommonConstant;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.common.BaseConfig;
import io.sermant.core.config.strategy.LoadConfigStrategy;
import io.sermant.core.config.utils.ConfigFieldUtil;
import io.sermant.core.config.utils.ConfigKeyUtil;
import io.sermant.core.config.utils.ConfigValueUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * General implementation for properties files of {@link LoadConfigStrategy}, where main carrier of configuration
 * information is {@link Properties}
 * <p>The strategy obtains configuration file information through file flow and requires that the configuration file
 * must be stored in the upper-level directory of the current jar package
 * <p>Support int、short、long、float、double、enumeration、String and Object. Also support arrays, lists, and maps
 * they make up
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-08-19
 */
public class LoadPropertiesStrategy implements LoadConfigStrategy<Properties> {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Collection parameter type length
     */
    private static final int COLLECTION_ARGUMENT_TYPE_LEN = 1;

    /**
     * MAP parameter type length
     */
    private static final int MAP_ARGUMENT_TYPE_LEN = 2;

    /**
     * argsMap
     */
    private Map<String, Object> argsMap;

    @Override
    public boolean canLoad(File file) {
        final String fileName = file.getName();
        return fileName.endsWith(".properties") || fileName.endsWith(".config") || fileName.endsWith(".conf");
    }

    /**
     * Obtain the configuration information of the configuration file by using the configuration file name and save it
     * in {@link Properties}
     * <p>The configuration file is required to reside in the sibling directory of the current jar package
     * <p>The final configuration information will be overwritten by {@code argsMap}, which has the highest priority
     *
     * @param config configuration file
     * @param bootArgsMap parameters set at startup
     * @return 配置信息承载对象
     */
    @Override
    public Properties getConfigHolder(File config, Map<String, Object> bootArgsMap) {
        this.argsMap = bootArgsMap;
        return readConfig(config);
    }

    /**
     * Load configuration object
     * <p>Use the ConfigTypeKey and ConfigFieldKey annotations to locate the configuration information for properties
     * <p>If the ConfigFieldKey does not exist, concatenate the field name of the configuration object directly
     * <p>When setting a configuration object field value, first look for {@code setter} calls, and try direct
     * assignment if none exists
     * <p>Therefore, the field value of the configuration object is required to have the corresponding {@code
     * setter}, or the field value is required to be public
     *
     * @param holder The main carrier of the configuration information
     * @param config Configuration object
     * @return Configure object generics
     */
    @Override
    public <R extends BaseConfig> R loadConfig(Properties holder, R config, boolean isDynamic) {
        return loadConfig(holder, config.getClass(), config);
    }

    private <R extends BaseConfig> R loadConfig(Properties holder, Class<?> cls, R config) {
        if (!BaseConfig.class.isAssignableFrom(cls)) {
            return config;
        }
        loadConfig(holder, cls.getSuperclass(), config);
        final String typeKey = ConfigKeyUtil.getTypeKey(cls);
        for (Field field : cls.getDeclaredFields()) {
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            final String key = typeKey + '.' + ConfigKeyUtil.getFieldKey(field);
            final Object value = getConfig(holder, key, field);
            if (value != null) {
                ConfigFieldUtil.setField(config, field, value);
            }
        }
        return config;
    }

    /**
     * Read configuration file
     *
     * @param config configuration file
     * @return configuration content
     */
    private Properties readConfig(File config) {
        final Properties properties = new Properties();
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(config), CommonConstant.DEFAULT_CHARSET);
            properties.load(reader);
        } catch (IOException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Missing config file [%s], please check.", config));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    LOGGER.warning("Unexpected exception occurs. ");
                }
            }
        }
        return properties;
    }

    /**
     * Obtain the configuration information
     *
     * @param config The main carrier of the configuration information {@link Properties}
     * @param key Configuration key
     * @param field Field
     * @return Configuration information
     */
    private Object getConfig(Properties config, String key, Field field) {
        final String configStr = getConfigStr(config, key);
        return configStr == null ? null : transType(configStr, field);
    }

    /**
     * Get the configuration value and fix the configuration in the shape of ${} using the {@link
     * ConfigValueUtil#fixValue} method
     *
     * @param config Properties
     * @param key Configuration key
     * @return Configuration value
     */
    private String getConfigStr(Properties config, String key) {
        Object arg = argsMap.get(key);
        String configVal;
        if (arg == null) {
            configVal = config.getProperty(key);
            if (configVal == null) {
                configVal = readMapOrCollection(null, config, key);
            }
        } else {
            configVal = arg.toString();
        }
        return ConfigValueUtil.fixValue(key, configVal, argsMap,
                new ConfigValueUtil.FixedValueProvider() {
                    @Override
                    public String getFixedValue(String key) {
                        return config.getProperty(key);
                    }
                });
    }

    /**
     * Match Map/List/Set, also support: (1) xxx.mapName.key1=value1 for Map (2) xxx.xxx.listName[0]=elem1 for List or
     * Set
     *
     * @param configVal Configuration value
     * @param config Configuration
     * @param key Configuration key
     * @return Configuration value
     */
    public String readMapOrCollection(String configVal, Properties config, String key) {
        String result = configVal;
        StringBuilder sb = new StringBuilder();
        for (String propertyName : config.stringPropertyNames()) {
            if (propertyName.startsWith(key)) {
                // Match List and Set
                if (propertyName.matches("(.*)\\[[0-9]*]$")) {
                    sb.append(config.getProperty(propertyName))
                            .append(CommonConstant.COMMA);
                } else {
                    // Match Map
                    sb.append(propertyName.replace(key + CommonConstant.DOT, ""))
                            .append(CommonConstant.COLON)
                            .append(config.getProperty(propertyName))
                            .append(CommonConstant.COMMA);
                }
            }
        }
        if (sb.length() > 0) {
            result = sb.deleteCharAt(sb.length() - 1).toString();
        }
        return result;
    }

    /**
     * Type conversion: Converts the configuration information string by using methods such as {@link
     * ConfigValueUtil#toBaseType}
     * <p> * <p>Support int、short、long、float、double、enumeration、String and Object. Also support arrays, lists, and maps
     * they make upp
     *
     * @param configStr Configuration value
     * @param field field
     * @return Converted type
     */
    private Object transType(String configStr, Field field) {
        final Class<?> fieldType = field.getType();
        if (fieldType.isArray()) {
            return ConfigValueUtil.toArrayType(configStr, fieldType.getComponentType());
        } else if (List.class.equals(fieldType)) {
            final Type[] argumentTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            return (argumentTypes.length != COLLECTION_ARGUMENT_TYPE_LEN || !(argumentTypes[0] instanceof Class)) ? null
                    : ConfigValueUtil.toListType(configStr, (Class<?>) argumentTypes[0]);
        } else if (Map.class.equals(fieldType)) {
            final Type[] argumentTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            return (argumentTypes.length != MAP_ARGUMENT_TYPE_LEN || !(argumentTypes[0] instanceof Class)
                    || !(argumentTypes[1] instanceof Class)) ? null
                    : ConfigValueUtil.toMapType(configStr, (Class<?>) argumentTypes[0], (Class<?>) argumentTypes[1]);
        } else if (Set.class.equals(fieldType)) {
            final Type[] argumentTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            return (argumentTypes.length != COLLECTION_ARGUMENT_TYPE_LEN || !(argumentTypes[0] instanceof Class)) ? null
                    : ConfigValueUtil.toSetType(configStr, (Class<?>) argumentTypes[0]);
        } else {
            return ConfigValueUtil.toBaseType(configStr, fieldType);
        }
    }
}
