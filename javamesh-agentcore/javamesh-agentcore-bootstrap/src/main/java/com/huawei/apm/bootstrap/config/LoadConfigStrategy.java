/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.apm.bootstrap.config;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.huawei.apm.bootstrap.lubanops.log.LogFactory;

/**
 * 加载配置对象的策略
 * <p>加载配置信息主要分两步：
 * <pre>
 *     1.加载配置文件为配置信息{@link #getConfigHolder(String, Map)}
 *     2.将配置信息加载到配置对象中{@link #loadConfig(Object, BaseConfig)}
 * </pre>
 *
 * @author h30007557
 * @version 1.0.0
 * @since 2021/8/18
 */
public interface LoadConfigStrategy<T> {
    /**
     * 获取配置信息主要承载对象
     * <P>承载对象与配置文件格式有关，如properties文件之于{@code Properties}对象，xml之于{@code Document}对象
     * <p>需要实现类定制通过配置文件名获取配置信息主要承载对象的方式，一般有以下几种：
     * <pre>
     *     1.通过特定的配置文件目录或相对目录获取外部的配置文件
     *     2.获取内部resource资源中的配置文件
     *     3.通过网络socket流获取配置文件
     * </pre>
     * {@code argsMap}为启动时设定的参数，优先级最高，需要定制化处理
     *
     * @param configFileName 配置文件名称
     * @param argsMap        启动时设定的参数
     * @return 配置信息主要承载对象
     */
    T getConfigHolder(String configFileName, Map<String, String> argsMap);

    /**
     * 加载配置，将配置信息主要承载对象中的配置信息加载到配置对象中
     *
     * @param holder 配置信息主要承载对象
     * @param config 配置对象
     * @param <R>    配置对象类型
     */
    <R extends BaseConfig> void loadConfig(T holder, R config);

    /**
     * 默认的{@link LoadConfigStrategy}，不做任何逻辑操作
     */
    class DefaultLoadConfigStrategy implements LoadConfigStrategy<Object> {
        /**
         * 日志
         */
        private static final Logger LOGGER = LogFactory.getLogger();

        @Override
        public Object getConfigHolder(String configFileName, Map<String, String> argsMap) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "[%s] will do nothing when reading config file [%s]. ",
                    DefaultLoadConfigStrategy.class.getName(), configFileName));
            return argsMap;
        }

        @Override
        public <R extends BaseConfig> void loadConfig(Object holder, R config) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "[%s] will do nothing when loading config [%s]. ",
                    DefaultLoadConfigStrategy.class.getName(), config.getClass().getName()));
        }
    }
}
