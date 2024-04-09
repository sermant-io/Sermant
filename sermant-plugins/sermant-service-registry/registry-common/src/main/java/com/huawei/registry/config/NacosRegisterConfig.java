/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
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

package com.huawei.registry.config;

import com.huaweicloud.sermant.core.config.ConfigManager;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;
import com.huaweicloud.sermant.core.plugin.config.PluginConfigManager;
import com.huaweicloud.sermant.core.plugin.config.ServiceMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Nacos registration plugin configuration
 *
 * @author chengyouling
 * @since 2022-10-20
 */
@ConfigTypeKey(value = "nacos.service")
public class NacosRegisterConfig implements PluginConfig {
    /**
     * Default pull interval
     */
    private static final long DEFAULT_NOTIFY_DELAY = 5000L;

    /**
     * The default monitoring time
     */
    private static final long DEFAULT_LOOKUP_INTERVAL = 30L;

    /**
     * Default data page size
     */
    private static final int DEFAULT_PAGINATION_SIZE = 100;

    /**
     * spring cloud zone
     * If you do not configure a zone that uses system environment variables by default,
     * that is, spring.cloud.loadbalancer.zone
     */
    private String zone;

    /**
     * Whether it is encrypted or not
     */
    private boolean secure = false;

    /**
     * Nacos authentication Account
     */
    private String username;

    /**
     * Nacos authentication password
     */
    private String password;

    /**
     * Node address
     */
    private String endpoint = "";

    /**
     * Namespace
     */
    private String namespace;

    /**
     * The name of the nacos log file
     */
    private String logName;

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
     * Service name separator
     */
    private String serviceNameSeparator = ":";

    /**
     * Data page size
     */
    private int paginationSize = DEFAULT_PAGINATION_SIZE;

    /**
     * Monitor time
     */
    private long lookupInterval = DEFAULT_LOOKUP_INTERVAL;

    /**
     * Wake-up delay time
     */
    private long notifyDelay = DEFAULT_NOTIFY_DELAY;

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

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
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

    public String getServiceNameSeparator() {
        return serviceNameSeparator;
    }

    public void setServiceNameSeparator(String serviceNameSeparator) {
        this.serviceNameSeparator = serviceNameSeparator;
    }

    public int getPaginationSize() {
        return paginationSize;
    }

    public void setPaginationSize(int paginationSize) {
        this.paginationSize = paginationSize;
    }

    public long getLookupInterval() {
        return lookupInterval;
    }

    public void setLookupInterval(long lookupInterval) {
        this.lookupInterval = lookupInterval;
    }

    public long getNotifyDelay() {
        return notifyDelay;
    }

    public void setNotifyDelay(long notifyDelay) {
        this.notifyDelay = notifyDelay;
    }

    /**
     * Obtain the configuration parameters
     *
     * @return 配置
     */
    public Properties getNacosProperties() {
        RegisterServiceCommonConfig commonConfig =
                PluginConfigManager.getPluginConfig(RegisterServiceCommonConfig.class);
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, commonConfig.getAddress());
        properties.put(PropertyKeyConst.USERNAME, Objects.toString(username, ""));
        properties.put(PropertyKeyConst.PASSWORD, Objects.toString(password, ""));
        properties.put(PropertyKeyConst.NAMESPACE, Objects.toString(namespace, ""));
        properties.put(PropertyKeyConst.NACOS_NAMING_LOG_NAME, Objects.toString(logName, ""));
        if (endpoint.contains(PropertyKeyConst.HTTP_URL_COLON)) {
            int index = endpoint.indexOf(PropertyKeyConst.HTTP_URL_COLON);
            properties.put(PropertyKeyConst.ENDPOINT, endpoint.substring(0, index));
            properties.put(PropertyKeyConst.ENDPOINT_PORT, endpoint.substring(index + 1));
        } else {
            properties.put(PropertyKeyConst.ENDPOINT, endpoint);
        }
        properties.put(PropertyKeyConst.ACCESS_KEY, Objects.toString(accessKey, ""));
        properties.put(PropertyKeyConst.SECRET_KEY, Objects.toString(secretKey, ""));
        properties.put(PropertyKeyConst.CLUSTER_NAME, clusterName);
        properties.put(PropertyKeyConst.NAMING_LOAD_CACHE_AT_START, namingLoadCacheAtStart);
        return properties;
    }
}
