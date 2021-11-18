/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.core.config.strategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.yaml.snakeyaml.Yaml;

import com.huawei.apm.core.config.common.BaseConfig;
import com.huawei.apm.core.config.common.ConfigFieldKey;
import com.huawei.apm.core.config.utils.ConfigKeyUtil;
import com.huawei.apm.core.config.utils.ConfigValueUtil;
import com.huawei.apm.core.lubanops.bootstrap.log.LogFactory;

/**
 * yaml格式文件的加载策略
 * <p>yaml格式转换对于数组、List和Map中涉及的复杂对象，不支持{@link ConfigFieldKey}修正字段名
 * <p>yaml格式转换对于数组、List和Map中的字符串，不支持{@code ${}}转换，字符串和复杂类型支持支持
 * <p>yaml格式转换仅在字符串做{@code ${}}转换时使用入参，不支持使用入参直接设置字段值
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021/11/12
 */
public class LoadYamlStrategy implements LoadConfigStrategy<Map> {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

    private Map<String, Object> argsMap;

    /**
     * Yaml对象
     */
    private final Yaml yaml = new Yaml();

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

    /**
     * 读取文件内容
     *
     * @param config 配置文件
     * @return 配置信息
     */
    private Map<?, ?> readConfig(File config) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(config), Charset.forName("UTF-8")));
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

    @Override
    public <R extends BaseConfig> R loadConfig(Map holder, R config) {
        final Class<? extends BaseConfig> cls = config.getClass();
        final String typeKey = ConfigKeyUtil.getTypeKey(cls);
        final Object typeVal = holder.get(typeKey);
        if (!(typeVal instanceof Map)) {
            return config;
        }
        fixFieldKey((Map) typeVal, cls);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cls.getClassLoader());
            return (R) yaml.loadAs(yaml.dump(typeVal), cls);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    /**
     * 修正键，如果属性被{@link ConfigFieldKey}修饰，则将{@link ConfigFieldKey#value()}转化为属性值
     *
     * @param typeVal 类对应的配置信息
     * @param cls     类Class
     */
    private void fixFieldKey(Map typeVal, Class<?> cls) {
        if (cls == Object.class || Map.class.isAssignableFrom(cls)) {
            return;
        }
        fixFieldKey(typeVal, cls.getSuperclass());
        for (Field field : cls.getDeclaredFields()) {
            final ConfigFieldKey configFieldKey = field.getAnnotation(ConfigFieldKey.class);
            final String fieldKey = configFieldKey == null ? field.getName() : configFieldKey.value();
            final Object subTypeVal = configFieldKey == null ? typeVal.get(fieldKey) : typeVal.remove(fieldKey);
            if (subTypeVal instanceof Map) {
                fixFieldKey((Map) subTypeVal, field.getType());
                if (configFieldKey != null) {
                    typeVal.put(field.getName(), subTypeVal);
                }
            } else if (subTypeVal instanceof String && field.getType() == String.class) {
                final String fixedVal = ConfigValueUtil.fixValue(fieldKey, (String) subTypeVal, argsMap,
                        new ConfigValueUtil.FixedValueProvider() {
                            @Override
                            public String getFixedValue(String key) {
                                final Object fixedVal = typeVal.get(key);
                                if (fixedVal == null || fixedVal instanceof List || fixedVal instanceof Map) {
                                    return null;
                                }
                                return fixedVal.toString();
                            }
                        });
                typeVal.put(field.getName(), fixedVal);
            }
        }
    }
}
