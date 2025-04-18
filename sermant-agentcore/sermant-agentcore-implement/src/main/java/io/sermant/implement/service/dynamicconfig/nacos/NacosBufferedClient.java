/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.implement.service.dynamicconfig.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.auth.impl.NacosAuthLoginConstant;

import io.sermant.core.classloader.ClassLoaderManager;
import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.service.dynamicconfig.config.DynamicConfig;
import io.sermant.core.utils.AesUtil;
import io.sermant.core.utils.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link ConfigService} wrapper, which wraps the Nacos native apis and provides easier apis to use
 *
 * @author tangle
 * @since 2023-08-17
 */
public class NacosBufferedClient implements Closeable {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Dynamic configuration information
     */
    private static final DynamicConfig CONFIG = ConfigManager.getConfig(DynamicConfig.class);

    private NacosClient nacosClient;

    private Function<String, Map<String, List<String>>> groupKeysFunction;

    private final String namespace;

    /**
     * Create a NacosBufferedClient and initialize the Nacos client
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @param namespace namespace
     * @throws NacosInitException In the case of dependent dynamic configuration, if Nacos initialization fails then
     *                            Sermant needs to be interrupted
     */
    public NacosBufferedClient(String connectString, int sessionTimeout, String namespace) {
        this.namespace = namespace;
        Properties properties = createProperties(connectString, sessionTimeout);
        createNacosClient(connectString, properties);
        getGroupKeysFunction();
    }

    /**
     * Create a NacosBufferedClient and initialize the Nacos client
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @param namespace namespace
     * @param userName username
     * @param password encrypted password
     * @throws NacosInitException In the case of dependent dynamic configuration, if Nacos initialization fails then
     *                            Sermant needs to be interrupted
     */
    public NacosBufferedClient(String connectString, int sessionTimeout, String namespace, String userName,
                               String password) {
        this.namespace = namespace;
        Properties properties = createProperties(connectString, sessionTimeout, userName, password);
        createNacosClient(connectString, properties);
        getGroupKeysFunction();
    }

    /**
     * Get all keys for all Nacos groups
     *
     * @return A Map of the groups and all its keys
     */
    public Map<String, List<String>> getGroupKeys() {
        return groupKeysFunction.apply(this.namespace);
    }

    /**
     * Get configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @return configuration content
     */
    public String getConfig(String key, String group) {
        try {
            final String data = this.nacosClient.getConfig(key, group, CONFIG.getRequestTimeout());
            return data == null ? "" : data;
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, "Nacos getConfig exception, msg is: {0}", e.getMessage());
            return "";
        }
    }

    /**
     * Publish configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @param content configuration content
     * @return publish result
     */
    public boolean publishConfig(String key, String group, String content) {
        return this.nacosClient.publishConfig(key, group, content);
    }

    /**
     * Delete configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @return remove result
     */
    public boolean removeConfig(String key, String group) {
        return this.nacosClient.removeConfig(key, group);
    }

    /**
     * Add listener
     *
     * @param key configuration key
     * @param group configuration group
     * @param listener listener
     * @return add result
     */
    public boolean addListener(String key, String group, Listener listener) {
        try {
            this.nacosClient.addListener(key, group, listener);
            return true;
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, "Nacos addListener exception, msg is: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * Remove listener
     *
     * @param key configuration key
     * @param group configuration group
     * @param listener listener
     */
    public void removeListener(String key, String group, Listener listener) {
        this.nacosClient.removeListener(key, group, listener);
    }

    /**
     * Create properties
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @return Properties
     */
    private Properties createProperties(String connectString, int sessionTimeout) {
        Properties properties = new Properties();
        properties.setProperty(NacosAuthLoginConstant.SERVER, connectString);
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, connectString);
        properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
        properties.setProperty(PropertyKeyConst.CONFIG_LONG_POLL_TIMEOUT, String.valueOf(sessionTimeout));
        return properties;
    }

    /**
     * Create properties
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @param userName username
     * @param password encrypted password
     * @return Properties
     */
    private Properties createProperties(String connectString, int sessionTimeout, String userName, String password) {
        Properties properties = this.createProperties(connectString, sessionTimeout);
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password) || StringUtils.isEmpty(
                CONFIG.getPrivateKey())) {
            LOGGER.log(Level.SEVERE, "Nacos username, password or privateKey is Empty");
            return properties;
        }
        Optional<String> passWordOptinal = AesUtil.decrypt(CONFIG.getPrivateKey(), password);
        if (!passWordOptinal.isPresent()) {
            LOGGER.log(Level.SEVERE, "Nacos password parsing failed");
            return properties;
        }
        properties.setProperty(PropertyKeyConst.USERNAME, userName);
        properties.setProperty(PropertyKeyConst.PASSWORD, passWordOptinal.get());
        return properties;
    }

    /**
     * Create config service
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param properties properties
     * @throws NacosInitException Connect to Nacos failed
     */
    private void createNacosClient(String connectString, Properties properties) {
        ClassLoader tempClassLoader = ClassLoaderManager.getContextClassLoaderOrUserClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            nacosClient = new NacosClient(properties);
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, "Nacos connection exception, msg is: {0}", e.getMessage());
            throw new NacosInitException(connectString);
        } finally {
            Thread.currentThread().setContextClassLoader(tempClassLoader);
        }
    }

    private void getGroupKeysFunction() {
        try {
            if (nacosClient.hasHistoryConfigApi()) {
                groupKeysFunction = ns -> {
                    try {
                        return nacosClient.getGroupKeysWithoutContent(ns);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Nacos http request exception.");
                        return Collections.emptyMap();
                    }
                };
                return;
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "get group keys function has error.", ex);
        }
        groupKeysFunction = ns -> {
            try {
                return nacosClient.getGroupKeys(null, null, ns, true);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Nacos http request exception.");
                return Collections.emptyMap();
            }
        };
    }

    @Override
    public void close() {
        try {
            nacosClient.close();
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, "Nacos close exception, msg is: {0}", e.getMessage());
        }
    }
}
