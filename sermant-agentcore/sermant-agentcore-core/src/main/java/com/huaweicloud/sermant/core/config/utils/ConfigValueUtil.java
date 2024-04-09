/*
 * Copyright (C) 2021-2024 Sermant Authors. All rights reserved.
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

package com.huaweicloud.sermant.core.config.utils;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.exception.DupConfIndexException;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tool for processing parameter values in the configuration
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-16
 */
public class ConfigValueUtil {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * CONFIG_SEPARATOR
     */
    private static final String CONFIG_SEPARATOR = ",";

    /**
     * ENV_PREFIX_LEN
     */
    private static final int ENV_PREFIX_LEN = 2;

    /**
     * MAP_KV_LEN
     */
    private static final int MAP_KV_LEN = 2;

    /**
     * Humps match pattern
     */
    private static final Pattern PATTERN = Pattern.compile("[A-Z]");

    /**
     * Configure the key formatter to read for different environment variable formats
     * <p>If reading environment variables for service.meta.applicationName, will attempt to read from the following
     * variables, or take the default values</p>
     * <li>service.meta.applicationName</li>
     * <li>service_meta_applicationName</li>
     * <li>service-meta-applicationName</li>
     * <li>SERVICE.META.APPLICATIONNAME</li>
     * <li>SERVICE_META_APPLICATIONNAME</li>
     * <li>SERVICE-META-APPLICATIONNAME</li>
     * <li>service.meta.applicationname</li>
     * <li>service_meta_applicationname</li>
     * <li>service-meta-applicationname</li>
     */
    private static final KeyFormatter[] KEY_FORMATTERS = new KeyFormatter[]{
            key -> key,
            key -> key.replace('.', '_'),
            key -> key.replace('.', '-'),
            key -> key.toUpperCase(Locale.ROOT),
            key -> key.toUpperCase(Locale.ROOT).replace('.', '_'),
            key -> key.toUpperCase(Locale.ROOT).replace('.', '-'),
            key -> key.toLowerCase(Locale.ROOT),
            key -> key.toLowerCase(Locale.ROOT).replace('.', '_'),
            key -> key.toLowerCase(Locale.ROOT).replace('.', '-')
    };

    /**
     * function for obtaining reference config values
     * <p>Priority: Startup Configuration > Environment Variables > Startup Parameters > Configuration File</p>
     */
    private static final List<ValueReferFunction<String, Map<String, Object>, String>>
            VALUE_REFER_FUNCTION = Arrays.asList(
            (key, argsMap) -> {
                final Object config = argsMap.get(key);
                return config == null ? null : config.toString();
            },
            (key, argsMap) -> System.getenv(key),
            (key, argsMap) -> System.getProperty(key)
    );

    /**
     * function for obtaining reference config values
     * <p>Priority: Startup Configuration > Environment Variables > Startup Parameters > Configuration Files</p>
     */
    private static final List<ValueFixFunction<String, Map<String, Object>, FixedValueProvider, String>>
            VALUE_FIX_FUNCTIONS = Arrays.asList(
            (key, argsMap, provider) -> {
                final Object config = argsMap.get(key);
                return config == null ? null : config.toString();
            },
            (key, argsMap, provider) -> System.getenv(key),
            (key, argsMap, provider) -> System.getProperty(key),
            (key, argsMap, provider) -> provider == null ? null : provider.getFixedValue(key)
    );

    private ConfigValueUtil() {
    }

    /**
     * To convert a configuration information string to an array, note the following:
     * <pre>
     *     1.The data type of the array must can be convert by {@link #toBaseType}
     *     2.The configuration information string is as follows: {@code value,value1,value2}
     *     3.Return as an array, meaning that the configuration can be modified. It is recommended that the get method
     *     return a copy of it {@link java.util.Arrays#copyOf}
     * </pre>
     *
     * @param configStr configuration information string
     * @param type type
     * @return The converted array
     */
    public static Object toArrayType(String configStr, Class<?> type) {
        final String[] configSlices = configStr.split(CONFIG_SEPARATOR);
        final Object result = Array.newInstance(type, configSlices.length);
        for (int i = 0; i < configSlices.length; i++) {
            Array.set(result, i, toBaseType(configSlices[i].trim(), type));
        }
        return result;
    }

    /**
     * To convert the configuration information string to a List, note the following:
     * <pre>
     *     1.The data type of List must can be convert by {@link #toBaseType}, null is skipped
     *     2.The configuration information string is as follows: {@code value,value1,value2}
     *     3.The List returned is an immutable List. Do not attempt to modify the contents of the List in the
     *     configuration
     * </pre>
     *
     * @param configStr configuration information string
     * @param type type in List
     * @param <R> generic type of the data in List
     * @return The converted List
     */
    public static <R> List<R> toListType(String configStr, Class<R> type) {
        final List<R> result = new ArrayList<R>();
        parseConfigToCollection(configStr, type, result);
        return Collections.unmodifiableList(result);
    }

    /**
     * To convert the configuration information string to Set, note the following:
     * <pre>
     *     1.The data type of Set must be convertible by {@link #toBaseType}, null is skipped
     *     2.The configuration information string is as follows: {@code value,value1,value2}
     *     3.The Set returned is an immutable Set. Do not attempt to modify the contents of the Set in the configuration
     * </pre>
     *
     * @param configStr configuration information string
     * @param type type in Set
     * @param <R> generic type of the data in Set
     * @return The converted Set
     */
    public static <R> Set<R> toSetType(String configStr, Class<R> type) {
        final Set<R> result = new HashSet<>();
        parseConfigToCollection(configStr, type, result);
        return Collections.unmodifiableSet(result);
    }

    private static <R> void parseConfigToCollection(String configStr, Class<R> type, Collection<R> result) {
        for (String configSlice : configStr.split(CONFIG_SEPARATOR)) {
            final R obj = toBaseType(configSlice.trim(), type);
            if (obj == null) {
                LOGGER.log(Level.WARNING, buildTransformErrMsg(configSlice, type.getName()));
                continue;
            }
            result.add(obj);
        }
    }

    private static String buildTransformErrMsg(String configSlice, String typeName) {
        return String.format(Locale.ROOT, "Cannot transform [%s] to [%s].", configSlice, typeName);
    }

    /**
     * To convert the configuration information string to a Map, note the following:
     * <pre>
     *     1.Map key-value types must be convertible by {@link #toBaseType}, null is skipped
     *     2.The configuration information string is as follows: {@code key:value,key2:value2}
     *     3.If the string array length of the key-value pair split by {@code :} is not 2, the key-value pair is skipped
     *     4.The same key exists, the latter overrides the former
     *     5.The returned Map is an immutable Map. Do not attempt to modify the contents of the Map in the configuration
     * </pre>
     *
     * @param configStr configuration information string
     * @param keyType key type in Map
     * @param valueType value type in Map
     * @param <K> generic type of key in Map
     * @param <V> generic type of value in Map
     * @return The converted Map
     */
    public static <K, V> Map<K, V> toMapType(String configStr, Class<K> keyType, Class<V> valueType) {
        final Map<K, V> result = new HashMap<K, V>();
        for (String kvSlice : configStr.split(CONFIG_SEPARATOR)) {
            final String[] kvEntry = kvSlice.trim().split(":");
            if (kvEntry.length != MAP_KV_LEN) {
                LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "Wrong map type entry [%s].", kvSlice));
                continue;
            }
            final K key = toBaseType(kvEntry[0].trim(), keyType);
            if (key == null) {
                LOGGER.log(Level.WARNING, buildTransformErrMsg(kvEntry[0], keyType.getName()));
                continue;
            }
            final V value = toBaseType(kvEntry[1].trim(), valueType);
            if (value == null) {
                LOGGER.log(Level.WARNING, buildTransformErrMsg(kvEntry[1], valueType.getName()));
                continue;
            }
            result.put(key, value);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Converts configuration information strings to int, short, long, float, double, enumeration, String, and
     * Object, and returns null if conversion fails
     *
     * @param configStr configuration information string
     * @param type type of object
     * @param <R> generic type of object
     * @return configuration information
     */
    public static <R> R toBaseType(String configStr, Class<R> type) {
        Object result = null;
        if (configStr == null || "".equals(configStr)) {
            return (R) result;
        }
        if (type == int.class) {
            result = Integer.parseInt(configStr);
        } else if (type == Integer.class) {
            result = Integer.valueOf(configStr);
        } else if (type == short.class) {
            result = Short.parseShort(configStr);
        } else if (type == Short.class) {
            result = Short.valueOf(configStr);
        } else if (type == long.class) {
            result = Long.parseLong(configStr);
        } else if (type == Long.class) {
            result = Long.valueOf(configStr);
        } else if (type == float.class) {
            result = Float.parseFloat(configStr);
        } else if (type == Float.class) {
            result = Float.valueOf(configStr);
        } else if (type == double.class) {
            result = Double.parseDouble(configStr);
        } else if (type == Double.class) {
            result = Double.valueOf(configStr);
        } else if (type == boolean.class) {
            result = Boolean.parseBoolean(configStr);
        } else if (type == Boolean.class) {
            result = Boolean.valueOf(configStr);
        } else if (type.isEnum()) {
            result = Enum.valueOf((Class) type, configStr);
        } else if (type == String.class || type == Object.class) {
            result = configStr;
        } else {
            result = null;
        }
        return (R) result;
    }

    /**
     * Fixes a configuration in the shape of "${}", resolved as a configuration, environment variable, or system
     * variable
     *
     * @param configKey config key
     * @param configVal config value
     * @param argsMap argsMap
     * @param provider function provider
     * @return Modified configuration information string
     * @throws DupConfIndexException An exception occurred when configuring duplicate indexes
     */
    public static String fixValue(String configKey, String configVal, Map<String, Object> argsMap,
            FixedValueProvider provider) {
        if (configVal != null && configVal.matches("^.*\\$\\{[\\w.]+(:.*)?}.*$")) {
            final int startIndex = configVal.indexOf("${") + ENV_PREFIX_LEN;
            final int endIndex = configVal.indexOf('}', startIndex);
            final String envKey = configVal.substring(startIndex, endIndex);
            final int separatorIndex = envKey.indexOf(':');
            final String key = separatorIndex >= 0 ? envKey.substring(0, separatorIndex) : envKey;
            if (configKey.equals(key)) {
                throw new DupConfIndexException(key);
            }
            final String defaultValue = separatorIndex >= 0 ? envKey.substring(separatorIndex + 1) : "";
            final String value = getFormatKeyFixVal(key, defaultValue, argsMap, provider);
            return fixValue(configKey,
                    configVal.substring(0, startIndex - ENV_PREFIX_LEN) + value + configVal.substring(endIndex + 1),
                    argsMap, provider);
        } else {
            return getValByFixedKey(configKey, configVal, argsMap);
        }
    }

    /**
     * Get the modified field, priority: Startup Configuration > Environment Variables > Startup Parameters >
     * Configuration File > Default Value
     * <p>Fixed getting values in different configuration formats, including '-','_','.', upper and lower case</p>
     *
     * @param key key
     * @param defaultVal default value
     * @param provider configuration information
     * @param argsMap argsMap
     * @return Environment variable or system variable
     */
    private static String getFormatKeyFixVal(String key, String defaultVal, Map<String, Object> argsMap,
            FixedValueProvider provider) {
        for (ValueFixFunction<String, Map<String, Object>, FixedValueProvider, String> function : VALUE_FIX_FUNCTIONS) {
            for (KeyFormatter keyFormatter : KEY_FORMATTERS) {
                final String fixedValue = function.apply(keyFormatter.format(key), argsMap, provider);
                if (!StringUtils.isBlank(fixedValue)) {
                    return fixedValue;
                }
            }
        }
        return defaultVal;
    }

    /**
     * Get the modified field, priority: Startup Configuration > Environment Variables > Startup Parameters >
     * Configuration File
     * <p>Fixed getting values in different configuration formats, including '-','_','.', upper and lower case</p>
     *
     * @param key key
     * @param configVal default value
     * @param argsMap argsMap
     * @return The final configuration reference value
     */
    private static String getValByFixedKey(String key, String configVal, Map<String, Object> argsMap) {
        // appName is obtained directly, app-name is processed as app.name and then obtained
        String keyReplaceMiddleLine = transFromMiddleLine(key);
        Optional<String> fixedValue = getValueByOrder(argsMap, keyReplaceMiddleLine);
        if (fixedValue.isPresent()) {
            return fixedValue.get();
        }

        // appName is split into app.name
        String keyWithoutCamel = transFromCamel(keyReplaceMiddleLine);
        if (!keyReplaceMiddleLine.equals(keyWithoutCamel)) {
            fixedValue = getValueByOrder(argsMap, keyWithoutCamel);
            if (fixedValue.isPresent()) {
                return fixedValue.get();
            }
        }
        return configVal;
    }

    /**
     * Environment variables are read after being processed by KeyFormatter. The priorities are: Startup
     * Configuration > Environment Variables > Startup Parameters > Configuration File
     *
     * @param key key
     * @param argsMap argsMap
     * @return The final configuration reference value
     */
    private static Optional<String> getValueByOrder(Map<String, Object> argsMap, String key) {
        Optional<String> fixedValue;
        for (ValueReferFunction<String, Map<String, Object>, String> function : VALUE_REFER_FUNCTION) {
            for (KeyFormatter keyFormatter : KEY_FORMATTERS) {
                fixedValue = Optional.ofNullable(function.apply(keyFormatter.format(key), argsMap));
                if (fixedValue.isPresent()) {
                    return fixedValue;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Configuration key split into camel words
     * <p>Before reading environment variables，transform service.meta.applicationName to service.meta.application
     * .name then process by the KeyFormatter</p>
     *
     * @param key key which needs to be processed
     * @return key processed
     */
    private static String transFromCamel(String key) {
        Matcher matcher = PATTERN.matcher(key);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "." + matcher.group(0).toLowerCase(Locale.ROOT));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Configuration key hyphen formatting
     * <p>Before reading environment variables，transform service.meta.application-name to service.meta.application
     * .name then process by the KeyFormatter</p>
     *
     * @param key key which needs to be processed
     * @return key processed
     */
    private static String transFromMiddleLine(String key) {
        return key.replace("-", ".");
    }

    /**
     * Value correction, this class is only used to configure value correction
     *
     * @param <K> key
     * @param <B> bootstrapArgsMap
     * @param <P> sourceProvider
     * @param <R> result
     * @since 2022-07-05
     */
    interface ValueFixFunction<K, B, P, R> {
        /**
         * apply correction
         *
         * @param key key
         * @param bootstrapArgsMap bootstrapArgsMap
         * @param sourceProvider sourceProvider
         * @return value modified
         */
        R apply(K key, B bootstrapArgsMap, P sourceProvider);
    }

    /**
     * Configuration value reference, this class is used only for configuration value reference
     *
     * @param <K> key
     * @param <B> bootstrapArgsMap
     * @param <R> result
     * @since 2022-08-18
     */
    interface ValueReferFunction<K, B, R> {
        /**
         * apply reference
         *
         * @param key key
         * @param bootstrapArgsMap bootstrapArgsMap
         * @return reference value
         */
        R apply(K key, B bootstrapArgsMap);
    }

    /**
     * Configuration KeyFormatter
     *
     * @since 2021-11-16
     */
    @FunctionalInterface
    interface KeyFormatter {
        /**
         * Configuration formatting, identifying different configuration formats, including '-','_','.', and case
         *
         * @param key Original configuration key
         * @return The formatted key
         */
        String format(String key);
    }

    /**
     * Value correction provider
     *
     * @author HapThorin
     * @version 1.0.0
     * @since 2021-11-16
     */
    public interface FixedValueProvider {
        /**
         * Gets the corrected field
         *
         * @param key key
         * @return Corrected value
         */
        String getFixedValue(String key);
    }
}
