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
import com.huawei.sermant.core.config.common.ConfigTypeKey;
import com.huawei.sermant.core.config.utils.ConfigFieldUtil;
import com.huawei.sermant.core.config.utils.ConfigKeyUtil;
import com.huawei.sermant.core.config.utils.ConfigValueUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 加载配置对象策略{@link LoadConfigStrategy}的通用properties文件实现，其中主要配置信息承载对象为{@link Properties}
 * <p>该策略通过文件流获取配置文件信息，并要求配置文件必须存放在当前jar包的上级目录中
 * <p>属性支持int、short、long、float、double、枚举、String和Object类型，以及他们构成的数组、List和Map
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-08-19
 */
public class LoadPropertiesStrategy implements LoadConfigStrategy<Properties> {
    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 启动参数
     */
    private Map<String, Object> argsMap;

    @Override
    public boolean canLoad(File file) {
        final String fileName = file.getName();
        return fileName.endsWith(".properties") || fileName.endsWith(".config") || fileName.endsWith(".conf");
    }

    /**
     * 通过配置文件名获取配置文件的配置信息，并存放在{@link Properties}中
     * <p>要求配置文件必须存放在当前jar包的同级目录中
     * <p>最终的配置信息将被{@code argsMap}覆盖，{@code argsMap}拥有最高优先级
     *
     * @param config      配置文件
     * @param bootArgsMap 启动时设定的参数
     * @return 配置信息承载对象
     */
    @Override
    public Properties getConfigHolder(File config, Map<String, Object> bootArgsMap) {
        this.argsMap = bootArgsMap;
        return readConfig(config);
    }

    /**
     * 加载配置对象
     * <p>通过{@link ConfigTypeKey}和{@link ConfigFieldKey}注解定位到properties的配置信息
     * <p>如果{@link ConfigFieldKey}不存在，则直接使用配置对象的属性名拼接
     * <p>设置配置对象属性值时，优先查找{@code setter}调用，不存在时尝试直接赋值
     * <p>因此，要求配置对象的属性值需要拥有相应的{@code setter}，或者要求改属性值是公有的
     *
     * @param holder 配置信息主要承载对象
     * @param config 配置对象
     * @param <R>    配置对象泛型
     */
    @Override
    public <R extends BaseConfig> R loadConfig(Properties holder, R config) {
        return loadConfig(holder, config.getClass(), config);
    }

    private <R extends BaseConfig> R loadConfig(Properties holder, Class<?> cls, R config) {
        if (!BaseConfig.class.isAssignableFrom(cls)) {
            return config;
        }
        loadConfig(holder, cls.getSuperclass(), config);
        final String typeKey = ConfigKeyUtil.getTypeKey(cls);
        for (Field field : cls.getDeclaredFields()) {
            final String key = typeKey + '.' + ConfigKeyUtil.getFieldKey(field);
            final Object value = getConfig(holder, key, field);
            if (value != null) {
                ConfigFieldUtil.setField(config, field, value);
            }
        }
        return config;
    }

    /**
     * 读取配置文件
     *
     * @param config 配置文件对象
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
     * 获取配置信息内容
     *
     * @param config 配置主要承载对象{@link Properties}
     * @param key    配置键
     * @param field  属性
     * @return 配置信息
     */
    private Object getConfig(Properties config, String key, Field field) {
        final String configStr = getConfigStr(config, key);
        if (configStr == null) {
            return null;
        }
        return transType(configStr, field);
    }

    /**
     * 获取配置值，通过{@link ConfigValueUtil#fixValue}方法，修正形如"${}"的配置
     *
     * @param config 配置
     * @param key    配置键
     * @return 配置值
     */
    private String getConfigStr(Properties config, String key) {
        Object arg = argsMap.get(key);
        final String configVal;
        if (arg == null) {
            configVal = config.getProperty(key);
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
     * 类型转换，通过{@link ConfigValueUtil#toBaseType}等方法，将配置信息字符串进行类型转换
     * <p>支持int、short、long、float、double、枚举、String和Object类型，以及他们构成的数组、List和Map
     *
     * @param configStr 配置值
     * @param field     属性字段
     * @return 转换后的类型
     */
    private Object transType(String configStr, Field field) {
        final Class<?> fieldType = field.getType();
        if (fieldType.isArray()) {
            return ConfigValueUtil.toArrayType(configStr, fieldType.getComponentType());
        } else if (List.class.equals(fieldType)) {
            final Type[] argumentTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            if (argumentTypes.length != 1 || !(argumentTypes[0] instanceof Class)) {
                return null;
            }
            return ConfigValueUtil.toListType(configStr, (Class<?>) argumentTypes[0]);
        } else if (Map.class.equals(fieldType)) {
            final Type[] argumentTypes = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
            if (argumentTypes.length != 2 || !(argumentTypes[0] instanceof Class)
                    || !(argumentTypes[1] instanceof Class)) {
                return null;
            }
            return ConfigValueUtil.toMapType(configStr, (Class<?>) argumentTypes[0], (Class<?>) argumentTypes[1]);
        } else {
            return ConfigValueUtil.toBaseType(configStr, fieldType);
        }
    }
}
