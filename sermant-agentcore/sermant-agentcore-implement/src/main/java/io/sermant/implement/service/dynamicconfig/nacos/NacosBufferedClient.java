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

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.service.dynamicconfig.config.DynamicConfig;
import io.sermant.core.utils.AesUtil;
import io.sermant.core.utils.StringUtils;

import java.io.Closeable;
import java.util.Optional;
import java.util.Properties;
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
     * configService connection status type
     */
    public static final String KEY_CONNECTED = "UP";

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Dynamic configuration information
     */
    private static final DynamicConfig CONFIG = ConfigManager.getConfig(DynamicConfig.class);

    private ConfigService configService;

    /**
     * Create a NacosBufferedClient and initialize the Nacos client
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @param namespace namespace
     * @throws NacosInitException In the case of dependent dynamic configuration, if Nacos initialization fails then
     * Sermant needs to be interrupted
     */
    public NacosBufferedClient(String connectString, int sessionTimeout, String namespace) {
        Properties properties = createProperties(connectString, sessionTimeout, namespace);
        createConfigService(connectString, properties);
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
     * Sermant needs to be interrupted
     */
    public NacosBufferedClient(String connectString, int sessionTimeout, String namespace, String userName,
            String password) {
        Properties properties = createProperties(connectString, sessionTimeout, namespace, userName, password);
        createConfigService(connectString, properties);
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
            final String data = this.configService.getConfig(key, group, CONFIG.getRequestTimeout());
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
        try {
            return this.configService.publishConfig(key, group, content);
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, "Nacos publishConfig exception, msg is: {0}", e.getMessage());
            return false;
        }
    }

    /**
     * Delete configuration
     *
     * @param key configuration key
     * @param group configuration group
     * @return remove result
     */
    public boolean removeConfig(String key, String group) {
        try {
            return this.configService.removeConfig(key, group);
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, "Nacos removeConfig exception, msg is: {0}", e.getMessage());
            return false;
        }
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
            this.configService.addListener(key, group, listener);
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
        this.configService.removeListener(key, group, listener);
    }

    /**
     * Create properties
     *
     * @param connectString connect string, must be in the following format: {@code host:port[(,host:port)...]}
     * @param sessionTimeout session timeout
     * @param namespace namespace
     * @return Properties
     */
    private Properties createProperties(String connectString, int sessionTimeout, String namespace) {
        Properties properties = new Properties();
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
     * @param namespace namespace
     * @param userName username
     * @param password encrypted password
     * @return Properties
     */
    private Properties createProperties(String connectString, int sessionTimeout, String namespace, String userName,
            String password) {
        Properties properties = this.createProperties(connectString, sessionTimeout, namespace);
        if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(password) || StringUtils.isEmpty(
                CONFIG.getPrivateKey())) {
            LOGGER.log(Level.SEVERE, "Nacos username, password or privateKey is Empty");
            return properties;
        }
        Optional<String> userNameOptinal = AesUtil.decrypt(CONFIG.getPrivateKey(), userName);
        Optional<String> passWordOptinal = AesUtil.decrypt(CONFIG.getPrivateKey(), password);
        if (!userNameOptinal.isPresent() || !passWordOptinal.isPresent()) {
            LOGGER.log(Level.SEVERE, "Nacos username and password parsing failed");
            return properties;
        }
        properties.setProperty(PropertyKeyConst.USERNAME, userNameOptinal.get());
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
    private void createConfigService(String connectString, Properties properties) {
        try {
            if (!connect(properties)) {
                LOGGER.log(Level.SEVERE, "Nacos connection reaches the maximum number of retries");
                throw new NacosInitException(connectString);
            }
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, "Nacos connection exception, msg is: {0}", e.getMessage());
            throw new NacosInitException(connectString);
        }
    }

    /**
     * Connect to Nacos
     *
     * @param properties Nacos Client connection configuration
     * @return Nacos connection status
     * @throws NacosException Nacos initialization exception
     */
    private boolean connect(Properties properties) throws NacosException {
        int tryNum = 0;
        while (tryNum++ <= CONFIG.getConnectRetryTimes()) {
            // Nacos client initialization gets the current thread's classloader, which needs to be changed and then
            // changed back to the original classloader
            ClassLoader tempClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            configService = NacosFactory.createConfigService(properties);
            Thread.currentThread().setContextClassLoader(tempClassLoader);
            if (KEY_CONNECTED.equals(configService.getServerStatus())) {
                return true;
            }
            try {
                Thread.sleep(CONFIG.getConnectTimeout());
                LOGGER.log(Level.INFO, "The {0} times to retry to connect to nacos", tryNum);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Nacos connection sleep exception, msg is: {0}", e.getMessage());
            }
        }
        return false;
    }

    @Override
    public void close() {
        try {
            configService.shutDown();
        } catch (NacosException e) {
            LOGGER.log(Level.SEVERE, "Nacos close exception, msg is: {0}", e.getMessage());
        }
    }
}