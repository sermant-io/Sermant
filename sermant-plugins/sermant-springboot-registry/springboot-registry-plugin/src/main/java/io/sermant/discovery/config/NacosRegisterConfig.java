/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package io.sermant.discovery.config;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.ConfigManager;
import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.config.ServiceMeta;
import io.sermant.core.utils.AesUtil;
import io.sermant.core.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * nacos Register plug-in configuration
 *
 * @author xiaozhao
 * @since 2024-11-16
 */
@ConfigTypeKey(value = "nacos.config")
public class NacosRegisterConfig implements PluginConfig {
    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * spring boot zone
     * If you do not configure a zone that uses system environment variables by default,
     */
    private String zone;

    /**
     * Whether it is encrypted or not
     */
    private boolean secure = false;

    /**
     * Whether to enable authentication
     */
    private boolean enableAuth;

    /**
     * Nacos authentication Account
     */
    private String username;

    /**
     * Nacos authentication password
     */
    private String password;

    /**
     * privateKey
     */
    private String privateKey;

    /**
     * Namespace
     */
    private String namespace;

    /**
     * The weight of the service instance
     */
    private float weight = 1f;

    /**
     * The name of the cluster
     */
    private String clusterName = "DEFAULT";

    /**
     * Group
     */
    private String group = "DEFAULT_GROUP";

    /**
     * Whether to load the cache at startup
     */
    private String namingLoadCacheAtStart = "false";

    /**
     * Namespace AK
     */
    private String accessKey;

    /**
     * Namespace SK
     */
    private String secretKey;

    /**
     * Whether the instance is available
     */
    private boolean instanceEnabled = true;

    /**
     * Whether it is a temporary node
     */
    private boolean ephemeral = true;

    /**
     * Instance metadata
     */
    private Map<String, String> metadata = new HashMap<>();

    /**
     * Whether it fails to retrieve cached data quickly, if false is not retrieved, it will fail directly
     */
    private boolean failureToleranceEnabled = false;

    /**
     * Constructor
     */
    public NacosRegisterConfig() {
        final ServiceMeta serviceMeta = ConfigManager.getConfig(ServiceMeta.class);
        if (serviceMeta == null) {
            return;
        }
        zone = serviceMeta.getZone();
        group = serviceMeta.getApplication();
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getNamingLoadCacheAtStart() {
        return namingLoadCacheAtStart;
    }

    public void setNamingLoadCacheAtStart(String namingLoadCacheAtStart) {
        this.namingLoadCacheAtStart = namingLoadCacheAtStart;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isInstanceEnabled() {
        return instanceEnabled;
    }

    public void setInstanceEnabled(boolean instanceEnabled) {
        this.instanceEnabled = instanceEnabled;
    }

    public boolean isEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public boolean isFailureToleranceEnabled() {
        return failureToleranceEnabled;
    }

    public void setFailureToleranceEnabled(boolean failureToleranceEnabled) {
        this.failureToleranceEnabled = failureToleranceEnabled;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public boolean isEnableAuth() {
        return enableAuth;
    }

    public void setEnableAuth(boolean enableAuth) {
        this.enableAuth = enableAuth;
    }

    /**
     * Get configuration parameters
     *
     * @return Properties
     */
    public Properties getNacosProperties() {
        LbConfig lbConfig = PluginConfigManager.getPluginConfig(LbConfig.class);
        Properties properties = new Properties();
        if (enableAuth) {
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)
                    || StringUtils.isEmpty(privateKey)) {
                LOGGER.log(Level.SEVERE, "Nacos username, password or privateKey is Empty");
                return properties;
            }
            Optional<String> pwd = AesUtil.decrypt(privateKey, password);
            if (!pwd.isPresent()) {
                LOGGER.log(Level.SEVERE, "Nacos password parsing failed");
                return properties;
            }
            properties.put(PropertyKeyConst.USERNAME, username);
            properties.put(PropertyKeyConst.PASSWORD, pwd.get());
        }
        properties.put(PropertyKeyConst.SERVER_ADDR, lbConfig.getRegistryAddress());
        properties.put(PropertyKeyConst.NAMESPACE, Objects.toString(namespace, ""));
        properties.put(PropertyKeyConst.ACCESS_KEY, Objects.toString(accessKey, ""));
        properties.put(PropertyKeyConst.SECRET_KEY, Objects.toString(secretKey, ""));
        properties.put(PropertyKeyConst.CLUSTER_NAME, clusterName);
        properties.put(PropertyKeyConst.NAMING_LOAD_CACHE_AT_START, namingLoadCacheAtStart);
        return properties;
    }
}
