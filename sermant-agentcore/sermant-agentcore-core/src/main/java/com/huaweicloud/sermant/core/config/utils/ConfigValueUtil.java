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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 对配置中参数值进行处理的工具
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-16
 */
public class ConfigValueUtil {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 配置分隔符
     */
    private static final String CONFIG_SEPARATOR = ",";

    /**
     * 环境前缀长度
     */
    private static final int ENV_PREFIX_LEN = 2;

    /**
     * Map kv长度
     */
    private static final int MAP_KV_LEN = 2;

    /**
     * 配置键格式化器, 针对不同环境变量格式读取
     * <p>若读取环境变量 service.meta.applicationName, 则会尝试从下面的变量进行读取， 否则取默认值</p>
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
        key -> key.toLowerCase(Locale.ROOT).replace('.', '-'),
    };

    /**
     * 值获取表达式
     * <p>优先级: 启动配置 > 环境变量 > 启动参数 > 配置文件</p>
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
     * 将配置信息字符串转换为数组，需要注意以下内容：
     * <pre>
     *     1.数组的数据类型必须可被{@link #toBaseType}转换
     *     2.配置信息字符串形如：{@code value,value1,value2}
     *     3.以数组的形式返回，意味着该配置可以被修改，建议get方法返回它的复制{@link java.util.Arrays#copyOf}
     * </pre>
     *
     * @param configStr 配置信息字符串
     * @param type 数组的数据类型
     * @return 转换后的数组
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
     * 将配置信息字符串转换为List，需要注意以下内容：
     * <pre>
     *     1.List的数据类型必须可被{@link #toBaseType}转换，空值跳过
     *     2.配置信息字符串形如：{@code value,value1,value2}
     *     3.返回的List为不可变List，不要尝试修改配置中List的内容
     * </pre>
     *
     * @param configStr 配置信息字符串
     * @param type List中数据的类型
     * @param <R> List中数据的泛型
     * @return 转换后的List
     */
    public static <R> List<R> toListType(String configStr, Class<R> type) {
        final List<R> result = new ArrayList<R>();
        parseConfigToCollection(configStr, type, result);
        return Collections.unmodifiableList(result);
    }

    /**
     * 将配置信息字符串转换为Set，需要注意以下内容：
     * <pre>
     *     1.Set的数据类型必须可被{@link #toBaseType}转换，空值跳过
     *     2.配置信息字符串形如：{@code value,value1,value2}
     *     3.返回的Set为不可变Set，不要尝试修改配置中Set的内容
     * </pre>
     *
     * @param configStr 配置信息字符串
     * @param type Set中数据的类型
     * @param <R> Set中数据的泛型
     * @return 转换后的Set
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
     * 将配置信息字符串转换为Map，需要注意以下内容：
     * <pre>
     *     1.Map的键值类型必须可被{@link #toBaseType}转换，空值跳过
     *     2.配置信息字符串形如：{@code key:value,key2:value2}
     *     3.如果{@code :}分割的键值对字符串数组长度不为2时，将跳过该键值对
     *     4.如果存在相同的键，后者将覆盖前者
     *     5.返回的Map为不可变Map，不要尝试修改配置中Map的内容
     * </pre>
     *
     * @param configStr 配置信息字符串
     * @param keyType Map的键类型
     * @param valueType Map的值类型
     * @param <K> Map的键泛型
     * @param <V> Map的值泛型
     * @return 转换后的Map
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
     * 将配置信息字符串进行类型转换，支持int、short、long、float、double、枚举、String和Object类型，转换失败时返回null
     *
     * @param configStr 配置信息字符串
     * @param type 配置对象属性类型
     * @param <R> 配置对象属性泛型
     * @return 配置信息
     */
    public static <R> R toBaseType(String configStr, Class<R> type) {
        Object result = null;
        if (configStr == null) {
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
     * 修正形如"${}"的配置，解析为配置、环境变量或系统变量
     *
     * @param configKey 配置信息键
     * @param configVal 配置信息字符串
     * @param argsMap 入参
     * @param provider 修正值获取方式
     * @return 修正后的配置信息字符串
     * @throws DupConfIndexException 配置重复索引异常
     */
    public static String fixValue(String configKey, String configVal, Map<String, Object> argsMap,
            FixedValueProvider provider) {
        if (configVal == null) {
            return configVal;
        }
        if (configVal.matches("^.*\\$\\{[\\w.]+(:.*)?}.*$")) {
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
            final String valFromEnv = getFormatKeyFixVal(configKey, configVal, argsMap, provider);
            if (valFromEnv != null) {
                return valFromEnv;
            }
        }
        return configVal;
    }

    /**
     * 获取修正的字段，优先级：入参 > 环境变量 > 系统变量 > 配置  > 默认值
     * <p>修正不同配置格式获取值, 含'-','_','.',大写以及小写</p>
     *
     * @param key 键
     * @param defaultVal 默认值
     * @param provider 配置信息
     * @return 环境变量或系统变量
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
     * 通过环境变量或者系统变量获取 环境变量 > 系统变量
     *
     * @param key 配置键
     * @return 变脸值
     */
    private static String getValFromEnv(String key) {
        final String envVal = System.getenv(key);
        if (envVal != null) {
            return envVal;
        }
        return System.getProperty(key);
    }

    /**
     * 值修正, 该类仅用于配置值修正
     *
     * @param <K> 键
     * @param <B> 源数据1
     * @param <P> 源数据2
     * @param <R> 结果
     * @since 2022-07-05
     */
    interface ValueFixFunction<K, B, P, R> {
        /**
         * 应用修正
         *
         * @param key 键
         * @param bootstrapArgsMap 启动参数Map
         * @param sourceProvider 配置数据提供
         * @return 修正后的值
         */
        R apply(K key, B bootstrapArgsMap, P sourceProvider);
    }

    /**
     * 配置键格式化器
     *
     * @since 2021-11-16
     */
    @FunctionalInterface
    interface KeyFormatter {
        /**
         * 配置格式化, 识别不同配置格式, 包含'-','_','.'以及大小写
         *
         * @param key 原配置键
         * @return 格式化之后的key
         */
        String format(String key);
    }

    /**
     * 值更正
     *
     * @author HapThorin
     * @version 1.0.0
     * @since 2021-11-16
     */
    public interface FixedValueProvider {
        /**
         * 获取修正的字段
         *
         * @param key 键
         * @return 修正后的字段
         */
        String getFixedValue(String key);
    }
}
