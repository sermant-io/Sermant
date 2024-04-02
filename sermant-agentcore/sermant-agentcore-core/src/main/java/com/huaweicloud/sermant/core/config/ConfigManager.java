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

package com.huaweicloud.sermant.core.config;

import com.huaweicloud.sermant.core.classloader.ClassLoaderManager;
import com.huaweicloud.sermant.core.common.BootArgsIndexer;
import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.common.BaseConfig;
import com.huaweicloud.sermant.core.config.strategy.LoadConfigStrategy;
import com.huaweicloud.sermant.core.config.utils.ConfigKeyUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuration manager
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2021-08-26
 */
public abstract class ConfigManager {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * CONFIG_MAP, key is the implementation Class of the configuration object, and the value is the loaded
     * configuration object
     * <p>Get the configuration object using the {@link #getConfig(Class)} method
     */
    private static final Map<String, BaseConfig> CONFIG_MAP = new HashMap<>();

    private static final List<LoadConfigStrategy> LOAD_CONFIG_STRATEGIES = new ArrayList<>();

    private static Map<String, Object> argsMap;

    /**
     * Close the configuration manager
     */
    public static void shutdown() {
        CONFIG_MAP.clear();
        LOAD_CONFIG_STRATEGIES.clear();
    }

    /**
     * The configuration object is obtained by configuration object type
     *
     * @param cls Configuration object type
     * @param <R> Configuration object generic type
     * @return Configuration object
     */
    public static <R extends BaseConfig> R getConfig(Class<R> cls) {
        return (R) CONFIG_MAP.get(ConfigKeyUtil.getTypeKey(cls));
    }

    /**
     * Performing initialization mainly involves the following steps
     * <pre>
     *     1.Manage boot configuration parameters
     *     2.Gets the loading configuration strategy
     *     3.Load configuration file
     *     4.Finds all configuration objects
     *     5.Load all configuration objects
     *     6.Keep all loaded configuration objects in {@code CONFIG_MAP}
     * </pre>
     * <p>If the loading configuration strategy does not exist, the default loading strategy is used and no
     * configuration loading is performed
     * <p>When the configuration file does not exist, only the content in {@code agentArgs} is processed as the
     * configuration information object
     * <p>Failure to encapsulate some configuration objects does not affect their saving in {@code CONFIG_MAP}, nor
     * does it affect the encapsulation of other configuration objects
     *
     * @param args Startup configuration parameter
     */
    public static synchronized void initialize(Map<String, Object> args) {
        argsMap = args;
        for (LoadConfigStrategy<?> strategy : ServiceLoader.load(LoadConfigStrategy.class,
                ClassLoaderManager.getFrameworkClassLoader())) {
            LOAD_CONFIG_STRATEGIES.add(strategy);
        }
        loadConfig(BootArgsIndexer.getConfigFile(), ClassLoaderManager.getSermantClassLoader());
    }

    /**
     * Load the configuration file and read the configuration information to the configuration object
     *
     * @param configFile configuration file
     * @param classLoader classLoader, which determines from which class Loader api operations are performed
     */
    protected static void loadConfig(File configFile, ClassLoader classLoader) {
        if (configFile.exists() && configFile.isFile()) {
            doLoadConfig(configFile, classLoader);
        } else {
            loadDefaultConfig(classLoader);
        }
    }

    /**
     * Load default configuration
     *
     * @param classLoader classLoader, which determines from which class Loader api operations are performed
     */
    private static synchronized void loadDefaultConfig(ClassLoader classLoader) {
        foreachConfig(new ConfigConsumer() {
            @Override
            public void accept(BaseConfig config) {
                final String typeKey = ConfigKeyUtil.getTypeKey(config.getClass());
                if (!CONFIG_MAP.containsKey(typeKey)) {
                    CONFIG_MAP.put(typeKey, config);
                }
            }
        }, classLoader);
    }

    /**
     * The configuration execution is loaded from the configuration file
     *
     * @param configFile configuration file
     * @param classLoader classloader. The loading strategy api is currently configured in the agentcore-implement
     * package, so use FrameworkClassLoader to load it
     */
    private static synchronized void doLoadConfig(File configFile,
            ClassLoader classLoader) {
        foreachConfig(config -> {
            final String typeKey = ConfigKeyUtil.getTypeKey(config.getClass());
            final BaseConfig retainedConfig = CONFIG_MAP.get(typeKey);
            if (retainedConfig == null) {
                CONFIG_MAP.put(typeKey, doLoad(configFile, config));
            } else if (retainedConfig.getClass() == config.getClass()) {
                LOGGER.fine(String.format(Locale.ROOT, "Skip load config [%s] repeatedly. ",
                        config.getClass().getName()));
            } else {
                LOGGER.warning(String.format(Locale.ROOT, "Type key of %s is %s, same as %s's. ",
                        config.getClass().getName(), typeKey, retainedConfig.getClass().getName()));
            }
        }, classLoader);
    }

    /**
     * Load configuration logic
     *
     * @param configFile configuration file
     * @param baseConfig base config
     * @return The configuration object after loading
     */
    public static BaseConfig doLoad(File configFile, BaseConfig baseConfig) {
        // Obtain configuration loading strategy using the FrameworkClassLoader
        final LoadConfigStrategy<?> loadConfigStrategy = getLoadConfigStrategy(configFile,
                ClassLoaderManager.getFrameworkClassLoader());
        final Object holder = loadConfigStrategy.getConfigHolder(configFile, argsMap);
        return ((LoadConfigStrategy) loadConfigStrategy).loadConfig(holder, baseConfig);
    }

    /**
     * Obtain loading configuration strategy by spi
     * <p>need to add the load configuration policy {@link LoadConfigStrategy} file in the {@code META-INF/services}
     * directory and type implementation
     * <p>If multiple implementations are declared, only the first one is valid
     * <p>If do not declare any implementation, using the default load strategy
     * {@link LoadConfigStrategy.DefaultLoadConfigStrategy}
     * <p>The default strategy does not perform any operations
     *
     * @param configFile configuration file
     * @param classLoader A classLoader for finding load strategy that allows new configuration load strategy to be
     * added to classLoader
     *
     * @return load strategy
     */
    private static LoadConfigStrategy<?> getLoadConfigStrategy(File configFile, ClassLoader classLoader) {
        for (LoadConfigStrategy<?> strategy : LOAD_CONFIG_STRATEGIES) {
            if (strategy.canLoad(configFile)) {
                return strategy;
            }
        }
        if (classLoader != ClassLoader.getSystemClassLoader()) {
            for (LoadConfigStrategy<?> strategy : ServiceLoader.load(LoadConfigStrategy.class, classLoader)) {
                if (strategy.canLoad(configFile)) {
                    return strategy;
                }
            }
        }
        LOGGER.log(Level.WARNING,
                String.format(Locale.ROOT, "Missing implement of [%s], use [%s].", LoadConfigStrategy.class.getName(),
                        LoadConfigStrategy.DefaultLoadConfigStrategy.class.getName()));
        return new LoadConfigStrategy.DefaultLoadConfigStrategy();
    }

    /**
     * Iterate over all configuration objects declared by spi
     * <p>need to add the configuration base class {@link BaseConfig} file in the {@code META-INF/services} directory
     * and add the implementation
     * <p>All implementations declared will be iterated, and each implementation class will get an
     * instance through spi, and then call {@code configConsumer} to consume
     *
     * @param configConsumer Configuration processing method
     * @param classLoader classLoader
     */
    private static void foreachConfig(ConfigConsumer configConsumer,
            ClassLoader classLoader) {
        for (BaseConfig config : ServiceLoader.load((Class<? extends BaseConfig>) BaseConfig.class, classLoader)) {
            configConsumer.accept(config);
        }
    }

    /**
     * ConfigConsumer
     *
     * @since 2021-12-31
     */
    public interface ConfigConsumer {
        /**
         * Handling BaseConfig
         *
         * @param config BaseConfig
         */
        void accept(BaseConfig config);
    }
}
