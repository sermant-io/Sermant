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

import com.huawei.sermant.core.common.LoggerFactory;
import com.huawei.sermant.core.config.common.BaseConfig;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 加载配置对象的策略
 * <p>加载配置信息主要分两步：
 * <pre>
 *     1.加载配置文件为配置信息{@link #getConfigHolder(File, Map)}
 *     2.将配置信息加载到配置对象中{@link #loadConfig(Object, BaseConfig)}
 * </pre>
 *
 * @param <T> 配置主体
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-08-18
 */
public interface LoadConfigStrategy<T> {
    /**
     * 能否加载
     *
     * @param file 配置文件
     * @return 能否加载
     */
    boolean canLoad(File file);

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
     * @param config  配置文件
     * @param argsMap 启动时设定的参数
     * @return 配置信息主要承载对象
     */
    T getConfigHolder(File config, Map<String, Object> argsMap);

    /**
     * 加载配置，将配置信息主要承载对象中的配置信息加载到配置对象中
     *
     * @param holder 配置信息主要承载对象
     * @param config 配置对象
     * @param <R>    配置对象类型
     * @return 配置对象
     */
    <R extends BaseConfig> R loadConfig(T holder, R config);

    /**
     * 默认的{@link LoadConfigStrategy}，不做任何逻辑操作
     */
    class DefaultLoadConfigStrategy implements LoadConfigStrategy<Object> {
        /**
         * 日志
         */
        private static final Logger LOGGER = LoggerFactory.getLogger();

        @Override
        public boolean canLoad(File file) {
            return false;
        }

        @Override
        public Object getConfigHolder(File config, Map<String, Object> argsMap) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "[%s] will do nothing when reading config file. ",
                    DefaultLoadConfigStrategy.class.getName()));
            return argsMap;
        }

        @Override
        public <R extends BaseConfig> R loadConfig(Object holder, R config) {
            LOGGER.log(Level.WARNING, String.format(Locale.ROOT, "[%s] will do nothing when loading config. ",
                    DefaultLoadConfigStrategy.class.getName()));
            return config;
        }
    }
}
