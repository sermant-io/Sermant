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

package com.huaweicloud.sermant.implement.config;

import com.huaweicloud.sermant.core.common.CommonConstant;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.common.BaseConfig;
import com.huaweicloud.sermant.core.config.common.ConfigFieldKey;
import com.huaweicloud.sermant.core.config.strategy.LoadConfigStrategy;
import com.huaweicloud.sermant.core.config.utils.ConfigKeyUtil;
import com.huaweicloud.sermant.core.config.utils.ConfigValueUtil;

import com.alibaba.fastjson.util.IOUtils;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;

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
 * yaml格式文件的加载策略
 * <p>yaml格式转换对于数组、List和Map中涉及的复杂对象，不支持{@link ConfigFieldKey}修正字段名
 * <p>yaml格式转换对于数组、List和Map中的字符串，不支持{@code ${}}转换，字符串和复杂类型支持
 * <p>yaml格式转换仅在字符串做{@code ${}}转换时使用入参，不支持使用入参直接设置字段值
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-11-12
 */
public class LoadYamlStrategy implements LoadConfigStrategy<Map> {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    private static final Map<Class<?>, Class<?>> BASE_TYPE_TRANSFER_MAP = new HashMap<Class<?>, Class<?>>() {{
            put(int.class, Integer.class);
            put(short.class, Short.class);
            put(long.class, Long.class);
            put(char.class, Character.class);
            put(byte.class, Byte.class);
            put(float.class, Float.class);
            put(double.class, Double.class);
            put(boolean.class, Boolean.class);
        }};

    /**
     * Yaml对象
     */
    private final Yaml yaml = new Yaml();
    /**
     * 启动参数
     */
    private Map<String, Object> argsMap;

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
        final Class<? extends BaseConfig> cls = config.getClass();
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
     * 读取文件内容
     *
     * @param config 配置文件
     * @return 配置信息
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
     * 修正键，如果属性被{@link ConfigFieldKey}修饰，则将{@link ConfigFieldKey#value()}转化为属性值
     *
     * @param typeMap 类对应的配置信息
     * @param cls     类Class
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
     * 修正值中形如"${}"的部分
     *
     * @param configKey  配置键
     * @param typeMap    父Map
     * @param subTypeVal 当前值
     * @return 修正后的值
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
                    fixedVal = yaml.loadAs(fixedStrValue, BASE_TYPE_TRANSFER_MAP.getOrDefault(field.getType(),
                            field.getType()));
                }
            } else {
                fixedVal = yaml.loadAs(ConfigValueUtil.fixValue(configKey, yaml.dump(subTypeVal), argsMap, provider),
                        subTypeVal.getClass());
            }
        } catch (ConstructorException exception) {
            LOGGER.severe(String.format(Locale.ENGLISH,"Error occurs while parsing configKey: %s", configKey));
        }

        return fixedVal;
    }
}
