/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lubanops.apm.bootstrap.log.LogFactory;
import com.lubanops.apm.premain.utils.LibPathUtils;

import com.huawei.apm.bootstrap.config.BaseConfig;
import com.huawei.apm.bootstrap.config.ConfigLoader;
import com.huawei.apm.bootstrap.config.LoadConfigStrategy;

/**
 * 加载配置对象策略{@link LoadConfigStrategy}的通用properties文件实现，其中主要配置信息承载对象为{@link Properties}
 * <p>该策略通过文件流获取配置文件信息，并要求配置文件必须存放在当前jar包的上级目录中
 *
 * @author h30007557
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
     * @param configFileName 配置文件名称
     * @param argsMap        启动时设定的参数
     * @return 配置信息承载对象
     */
    @Override
    public Properties getConfigHolder(String configFileName, Map<String, String> argsMap) {
        final Properties properties = new Properties();
        final String configPath = LibPathUtils.getAgentPath() + File.separator + configFileName;
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(configPath), Charset.forName("UTF-8"));
            properties.load(reader);
        } catch (IOException ignored) {
            LOGGER.log(Level.WARNING,
                    String.format(Locale.ROOT, "Missing config file [%s], please check.", configFileName));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        properties.putAll(argsMap);
        return properties;
    }

    /**
     * 加载配置对象
     * <p>通过{@link com.huawei.apm.bootstrap.config.ConfigTypeKey}和{@link
     * com.huawei.apm.bootstrap.config.ConfigFieldKey}注解定位到properties的配置信息
     * <p>如果{@link com.huawei.apm.bootstrap.config.ConfigFieldKey}不存在，则直接使用配置对象的属性名拼接
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
            final Object value = getConfig(holder, key, field.getType());
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
        final char head = fieldName.charAt(0);
        final String setterName = "set" + (
                (head >= 'a' && head <= 'z') ? ((char) (head + 'A' - 'a')) + fieldName.substring(1) : fieldName
        );
        try {
            return cls.getMethod(setterName, type);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * 获取配置信息内容，需要经过两次转换：
     * <pre>
     *     1.{@code fixConfig}方法，修正形如"${}"的配置
     *     2.{@code toBaseType}方法，将配置信息字符串进行类型转换
     * </pre>
     *
     * @param config 配置主要承载对象{@link Properties}
     * @param key    配置键
     * @param type   配置对象属性类型
     * @param <R>    配置对象属性泛型
     * @return 配置信息
     */
    private <R> R getConfig(Properties config, String key, Class<R> type) {
        return toBaseType(fixConfig(config.getProperty(key), config), type);
    }

    /**
     * 将配置信息字符串进行类型转换，支持int、short、long、float、double、String和Object类型
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
     * @param configStr 配置信息字符串
     * @return 修正后的配置信息字符串
     */
    private String fixConfig(String configStr, Properties config) {
        if (configStr != null && configStr.matches("^.*\\$\\{[\\w.:]+}.*$")) {
            final int startIndex = configStr.indexOf("${") + 2;
            final int endIndex = configStr.indexOf('}', startIndex);
            final String envKey = configStr.substring(startIndex, endIndex);
            final int separatorIndex = envKey.indexOf(':');
            final String key = separatorIndex >= 0 ? envKey.substring(0, separatorIndex) : envKey;
            final String defaultValue = separatorIndex >= 0 ? envKey.substring(separatorIndex + 1) : "";
            final String fixedValue = getFixedValue(key, defaultValue, config);
            return fixConfig(configStr.substring(0, startIndex - 2) + fixedValue
                    + configStr.substring(endIndex + 1), config);
        }
        return configStr;
    }

    /**
     * 获取修正的字段，优先级：配置 > 环境变量 > 系统变量 > 默认值
     *
     * @param key          键
     * @param defaultValue 默认值
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
