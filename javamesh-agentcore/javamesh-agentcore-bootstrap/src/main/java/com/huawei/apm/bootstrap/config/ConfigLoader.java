/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.bootstrap.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.apm.bootstrap.lubanops.commons.LubanApmConstants;
import com.huawei.apm.bootstrap.lubanops.log.LogFactory;

import com.huawei.apm.bootstrap.serialize.SerializerHolder;

/**
 * 配置储存器
 *
 * @author h30007557
 * @version 1.0.0
 * @since 2021/8/26
 */
public abstract class ConfigLoader {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogFactory.getLogger();

    /**
     * 配置对象集合，键为配置对象的实现类Class，值为加载完毕的配置对象
     * <p>通过{@link #getConfig(Class)}方法获取配置对象
     */
    private static final Map<String, BaseConfig> CONFIG_MAP = new HashMap<String, BaseConfig>();

    /**
     * 初始化标记
     */
    private static boolean initFlag = false;

    /**
     * 获取配置对象的键
     * <p>如果配置对象被{@link ConfigTypeKey}修饰，取其值
     * <p>如果不被{@link ConfigTypeKey}修饰，则取类的全限定名
     *
     * @param cls 配置对象类
     * @return 前缀字符串
     */
    public static String getTypeKey(Class<?> cls) {
        final ConfigTypeKey configTypeKey = cls.getAnnotation(ConfigTypeKey.class);
        if (configTypeKey == null) {
            return cls.getName();
        } else {
            return configTypeKey.value();
        }
    }

    /**
     * 获取配置信息键
     * <p>通过{@link ConfigFieldKey}注解获取成员属性对应配置信息键
     * <p>不存在注解时，直接取字段名
     *
     * @param field 字段
     * @return 配置信息键
     */
    public static String getFieldKey(Field field) {
        final ConfigFieldKey configFieldKey = field.getAnnotation(ConfigFieldKey.class);
        if (configFieldKey == null) {
            return field.getName();
        } else {
            return configFieldKey.value();
        }
    }

    /**
     * 通过配置对象类型获取配置对象
     *
     * @param cls 配置对象类型
     * @param <R> 配置对象泛型
     * @return 配置对象
     */
    public static <R extends BaseConfig> R getConfig(Class<R> cls) {
        final R config = getConfig(getTypeKey(cls), cls);
        if (config == null) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT,
                    "Missing configured instance of [%s], please check.", cls.getName()));
        }
        return config;
    }

    /**
     * 通过配置对象类型获取配置对象
     *
     * @param configKey 配置键
     * @param cls       配置对象类型
     * @param <R>       配置对象泛型
     * @return 配置对象
     */
    private static <R extends BaseConfig> R getConfig(String configKey, Class<R> cls) {
        final BaseConfig config = CONFIG_MAP.get(configKey);
        if (config == null) {
            return null;
        }
        if (cls == config.getClass()) {
            return (R) config;
        } else {
            return SerializerHolder.deserialize(SerializerHolder.serialize(config), cls);
        }
    }

    /**
     * 初始化，将{@code initFlag}修改为真
     *
     * @param agentArgs 启动配置参数
     */
    public static synchronized void initialize(String agentArgs, ClassLoader spiLoader) {
        if (initFlag) {
            return;
        }
        doInitialize(agentArgs, spiLoader);
        initFlag = true;
    }

    /**
     * 执行初始化，主要包含以下步骤：
     * <pre>
     *     1.处理启动配置参数
     *     2.获取加载配置策略
     *     3.加载配置文件
     *     4.查找所有配置对象
     *     5.加载所有配置对象
     *     6.将所有加载完毕的配置对象保留于{@code CONFIG_MAP}中
     * </pre>
     * <p>当加载配置策略不存在时，使用默认的加载策略，将不进行任何配置加载
     * <p>当配置文件不存在时，仅将{@code agentArgs}中的内容处理为配置信息承载对象
     * <p>当部分配置对象封装失败时，将不影响他们保存于{@code CONFIG_MAP}中，也不影响其他配置对象的封装
     *
     * @param agentArgs 启动配置参数
     */
    private static void doInitialize(String agentArgs, ClassLoader spiLoader) {
        final Map<String, String> argsMap = toArgsMap(agentArgs);
        final LoadConfigStrategy<?> loadConfigStrategy = getLoadConfigStrategy(spiLoader);
        final Object holder = loadConfigStrategy.getConfigHolder(LubanApmConstants.CONFIG_FILENAME, argsMap);
        foreachConfig(new ConfigConsumer() {
            @Override
            public void accept(BaseConfig config) {
                ((LoadConfigStrategy) loadConfigStrategy).loadConfig(holder, config);
                CONFIG_MAP.put(getTypeKey(config.getClass()), config);
            }
        }, spiLoader);
    }

    /**
     * 将启动配置参数字符串转换为map
     *
     * @param args 启动配置参数字符串
     * @return 配置参数map
     */
    private static Map<String, String> toArgsMap(String args) {
        final Map<String, String> argsMap = new HashMap<String, String>();
        for (String arg : args.trim().split(",")) {
            final int index = arg.indexOf('=');
            if (index >= 0) {
                argsMap.put(arg.substring(0, index).trim(), arg.substring(index + 1).trim());
            }
        }
        return argsMap;
    }

    /**
     * 通过spi的方式获取加载配置策略
     * <p>需要在{@code META-INF/services}目录中添加加载配置策略{@link LoadConfigStrategy}文件，并键入实现
     * <p>如果声明多个实现，仅第一个有效
     * <p>如果未声明任何实现，使用默认的加载配置策略{@link LoadConfigStrategy.DefaultLoadConfigStrategy}
     * <p>该默认策略不会进行任何业务操作
     *
     * @return 加载配置策略
     */
    private static LoadConfigStrategy<?> getLoadConfigStrategy(ClassLoader spiLoader) {
        for (LoadConfigStrategy<?> strategy : ServiceLoader.load(LoadConfigStrategy.class, spiLoader)) {
            return strategy;
        }
        LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "Missing implement of [%s], use [%s].",
                LoadConfigStrategy.class.getName(), LoadConfigStrategy.DefaultLoadConfigStrategy.class.getName()));
        return new LoadConfigStrategy.DefaultLoadConfigStrategy();
    }

    /**
     * 遍历所有通过spi方式声明的配置对象
     * <p>需要在{@code META-INF/services}目录中添加配置基类{@link BaseConfig}文件，并添加实现
     * <p>文件中声明的所有实现都将会进行遍历，每一个实现类都会通过spi获取实例，然后调用{@code configConsumer}进行消费
     *
     * @param configConsumer 配置处理方法
     */
    private static void foreachConfig(ConfigConsumer configConsumer, ClassLoader spiLoader) {
        for (BaseConfig config : ServiceLoader.load(BaseConfig.class, spiLoader)) {
            configConsumer.accept(config);
        }
    }

    public interface ConfigConsumer {
        void accept(BaseConfig config);
    }
}
