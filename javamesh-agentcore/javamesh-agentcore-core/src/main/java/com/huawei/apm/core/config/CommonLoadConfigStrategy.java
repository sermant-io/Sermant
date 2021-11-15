/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.apm.core.exception.ConfigDupIndexException;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

/**
 * 加载配置对象策略{@link LoadConfigStrategy}的通用properties文件实现，其中主要配置信息承载对象为{@link Properties}
 * <p>该策略通过文件流获取配置文件信息，并要求配置文件必须存放在当前jar包的上级目录中
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/8/19
 */
public class CommonLoadConfigStrategy implements LoadConfigStrategy<Properties> {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 通过配置文件名获取配置文件的配置信息，并存放在{@link Properties}中
     * <p>要求配置文件必须存放在当前jar包的同级目录中
     * <p>最终的配置信息将被{@code argsMap}覆盖，{@code argsMap}拥有最高优先级
     *
     * @param configs 配置文件集
     * @param argsMap 启动时设定的参数
     * @return 配置信息承载对象
     */
    @Override
    public Properties getConfigHolder(List<File> configs, Map<String, String> argsMap) {
        final Properties properties = new Properties();
        for (File config : configs) {
            readConfig(properties, config);
        }
        properties.putAll(argsMap);
        return properties;
    }

    /**
     * 读取配置文件
     *
     * @param properties 配置信息
     * @param config     配置文件对象
     */
    private void readConfig(Properties properties, File config) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(config), Charset.forName("UTF-8"));
            properties.load(reader);
        } catch (IOException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Missing config file [%s], please check.", config));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * 加载配置对象
     * <p>通过{@link com.huawei.apm.core.config.ConfigTypeKey}和{@link
     * com.huawei.apm.core.config.ConfigFieldKey}注解定位到properties的配置信息
     * <p>如果{@link com.huawei.apm.core.config.ConfigFieldKey}不存在，则直接使用配置对象的属性名拼接
     * <p>设置配置对象属性值时，优先查找{@code setter}调用，不存在时尝试直接赋值
     * <p>因此，要求配置对象的属性值需要拥有相应的{@code setter}，或者要求改属性值是公有的
     *
     * @param holder 配置信息主要承载对象
     * @param config 配置对象
     * @param <R>    配置对象泛型
     */
    @Override
    public <R extends BaseConfig> void loadConfig(Properties holder, R config) {
        final Class<? extends BaseConfig> cls = config.getClass();
        final String typeKey = ConfigLoader.getTypeKey(cls);
        for (Field field : cls.getDeclaredFields()) {
            final String key = typeKey + '.' + ConfigLoader.getFieldKey(field);
            final Object value = getConfig(holder, key, field);
            if (value != null) {
                setField(config, cls, field, value);
            }
        }
    }

    /**
     * 设置值，优先查找{@code setter}调用，不存在时尝试直接赋值
     * <p>因此，要求配置对象的属性值需要拥有相应的{@code setter}，或者要求改属性值是公有的
     *
     * @param obj   被设置值的对象
     * @param cls   被设置值的对象类型
     * @param field 被设置的字段
     * @param value 被设置的字段值
     */
    private void setField(Object obj, Class<?> cls, Field field, Object value) {
        try {
            final Method setter = getSetter(cls, field.getName(), field.getType());
            if (setter == null) {
                field.set(obj, value);
            } else {
                setter.invoke(obj, value);
            }
        } catch (IllegalAccessException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Cannot access field [%s] of [%s].", field.getName(), cls.getName()));
        } catch (InvocationTargetException ignored) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Failed to set field [%s] of [%s].", field.getName(), cls.getName()));
        }
    }

    /**
     * 通过属性名称获取{@code setter}
     *
     * @param cls       配置对象类
     * @param fieldName 属性名称
     * @param type      属性类型
     * @return setter方法
     */
    private Method getSetter(Class<?> cls, String fieldName, Class<?> type) {
        final String setterName;
        if ((type == boolean.class || type == Boolean.class) &&
                fieldName.length() >= 3 && (fieldName.startsWith("is") || fieldName.startsWith("Is")) &&
                fieldName.charAt(2) >= 'A' && fieldName.charAt(2) <= 'Z') {
            setterName = "set" + fieldName.substring(2);
        } else {
            final char head = fieldName.charAt(0);
            setterName = "set" + (
                    (head >= 'a' && head <= 'z') ? ((char) (head + 'A' - 'a')) + fieldName.substring(1) : fieldName);
        }
        try {
            return cls.getMethod(setterName, type);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * 获取配置信息内容，需要经过两次转换：
     * <pre>
     *     1.{@link #fixConfig}方法，修正形如"${}"的配置
     *     2.{@link #toBaseType}等方法，将配置信息字符串进行类型转换
     * </pre>
     * 支持int、short、long、float、double、枚举、String和Object类型，以及他们构成的数组、List和Map
     *
     * @param config 配置主要承载对象{@link Properties}
     * @param key    配置键
     * @param field  属性
     * @return 配置信息
     */
    private Object getConfig(Properties config, String key, Field field) {
        final String configStr = fixConfig(key, config.getProperty(key), config);
        final Class<?> fieldType = field.getType();
        if (fieldType.isArray()) {
            return toArrayType(configStr, fieldType.getComponentType());
        } else if (List.class.equals(fieldType)) {
            final Type[] argumentTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            if (argumentTypes.length != 1 || !(argumentTypes[0] instanceof Class)) {
                return null;
            }
            return toListType(configStr, (Class<?>) argumentTypes[0]);
        } else if (Map.class.equals(fieldType)) {
            final Type[] argumentTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            if (argumentTypes.length != 2 || !(argumentTypes[0] instanceof Class) ||
                    !(argumentTypes[1] instanceof Class)) {
                return null;
            }
            return toMapType(configStr, (Class<?>) argumentTypes[0], (Class<?>) argumentTypes[1]);
        } else {
            return toBaseType(configStr, fieldType);
        }
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
     * @param type      数组的数据类型
     * @return 转换后的数组
     */
    private Object toArrayType(String configStr, Class<?> type) {
        final String[] configSlices = configStr.split(",");
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
     * @param type      List中数据的类型
     * @param <R>       List中数据的泛型
     * @return 转换后的List
     */
    private <R> List<R> toListType(String configStr, Class<R> type) {
        final List<R> result = new ArrayList<R>();
        for (String configSlice : configStr.split(",")) {
            final R obj = toBaseType(configSlice.trim(), type);
            if (obj == null) {
                LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                        "Cannot transform [%s] to [%s].", configSlice, type.getName()));
                continue;
            }
            result.add(obj);
        }
        return Collections.unmodifiableList(result);
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
     * @param keyType   Map的键类型
     * @param valueType Map的值类型
     * @param <K>       Map的键泛型
     * @param <V>       Map的值泛型
     * @return 转换后的Map
     */
    private <K, V> Map<K, V> toMapType(String configStr, Class<K> keyType, Class<V> valueType) {
        final Map<K, V> result = new HashMap<K, V>();
        for (String kvSlice : configStr.split(",")) {
            final String[] kvEntry = kvSlice.trim().split(":");
            if (kvEntry.length != 2) {
                LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "Wrong map type entry [%s].", kvSlice));
                continue;
            }
            final K key = toBaseType(kvEntry[0].trim(), keyType);
            if (key == null) {
                LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                        "Cannot transform [%s] to [%s].", kvEntry[0], keyType.getName()));
                continue;
            }
            final V value = toBaseType(kvEntry[1].trim(), valueType);
            if (value == null) {
                LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                        "Cannot transform [%s] to [%s].", kvEntry[1], valueType.getName()));
                continue;
            }
            result.put(key, value);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * 修正形如"${}"的配置，解析为配置、环境变量或系统变量
     *
     * @param configKey 配置信息键
     * @param configStr 配置信息字符串
     * @param config    配置信息集
     * @return 修正后的配置信息字符串
     */
    private String fixConfig(String configKey, String configStr, Properties config) {
        if (configStr != null && configStr.matches("^.*\\$\\{[\\w.:]+}.*$")) {
            final int startIndex = configStr.indexOf("${") + 2;
            final int endIndex = configStr.indexOf('}', startIndex);
            final String envKey = configStr.substring(startIndex, endIndex);
            final int separatorIndex = envKey.indexOf(':');
            final String key = separatorIndex >= 0 ? envKey.substring(0, separatorIndex) : envKey;
            if (configKey.equals(key)) {
                throw new ConfigDupIndexException(key);
            }
            final String defaultValue = separatorIndex >= 0 ? envKey.substring(separatorIndex + 1) : "";
            final String fixedValue = getFixedValue(key, defaultValue, config);
            return fixConfig(configKey, configStr.substring(0, startIndex - 2) + fixedValue
                    + configStr.substring(endIndex + 1), config);
        }
        return configStr;
    }

    /**
     * 将配置信息字符串进行类型转换，支持int、short、long、float、double、枚举、String和Object类型，转换失败时返回null
     *
     * @param configStr 配置信息字符串
     * @param type      配置对象属性类型
     * @param <R>       配置对象属性泛型
     * @return 配置信息
     */
    private <R> R toBaseType(String configStr, Class<R> type) {
        if (configStr == null) {
            return null;
        }
        final Object result;
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
     * 获取修正的字段，优先级：配置 > 环境变量 > 系统变量 > 默认值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @param config       配置信息
     * @return 环境变量或系统变量
     */
    private String getFixedValue(String key, String defaultValue, Properties config) {
        String value = config.getProperty(key);
        if (value != null) {
            return value;
        }
        value = System.getenv(key);
        if (value != null) {
            return value;
        }
        value = System.getProperty(key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
}
