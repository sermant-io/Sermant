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

package com.huawei.sermant.core.config.strategy;

import com.huawei.sermant.core.common.CommonConstant;
import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.config.common.BaseConfig;
import com.huawei.sermant.core.config.common.ConfigFieldKey;
import com.huawei.sermant.core.config.utils.ConfigKeyUtil;
import com.huawei.sermant.core.config.utils.ConfigValueUtil;

import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
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
    public Map getConfigHolder(File config, Map<String, Object> argsMap) {
        this.argsMap = argsMap;
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
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cls.getClassLoader());
            return (R) yaml.loadAs(yaml.dump(fixEntry((Map) typeVal, cls)), cls);
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
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
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
        typeMap = fixEntry(typeMap, cls.getSuperclass());
        for (Field field : cls.getDeclaredFields()) {
            final ConfigFieldKey configFieldKey = field.getAnnotation(ConfigFieldKey.class);
            final String fieldKey = configFieldKey == null ? field.getName() : configFieldKey.value();
            final Object subTypeVal = configFieldKey == null ? typeMap.get(fieldKey) : typeMap.remove(fieldKey);
            if (subTypeVal == null) {
                continue;
            }
            final Object fixedVal;
            if (subTypeVal instanceof Map) {
                fixedVal = fixEntry((Map) subTypeVal, field.getType());
                if (configFieldKey != null) {
                    typeMap.put(field.getName(), fixedVal);
                }
            } else {
                fixedVal = fixValStr(typeMap, subTypeVal);
                typeMap.put(field.getName(), fixedVal);
            }
        }
        return typeMap;
    }

    /**
     * 修正值中形如"${}"的部分
     *
     * @param typeMap    父Map
     * @param subTypeVal 当前值
     * @return 修正后的值
     */
    private Object fixValStr(Map typeMap, Object subTypeVal) {
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
        final Object fixedVal;
        if (subTypeVal instanceof List) {
            fixedVal = yaml.loadAs(
                    ConfigValueUtil.fixValue("", yaml.dump(subTypeVal), argsMap, provider),
                    List.class);
        } else if (subTypeVal instanceof String) {
            fixedVal = ConfigValueUtil.fixValue("", (String) subTypeVal, argsMap, provider);
        } else {
            fixedVal = subTypeVal;
        }
        return fixedVal;
    }
}
