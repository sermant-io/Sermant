/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.discovery.config;

import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

import java.util.Collections;
import java.util.List;

/**
 * Load balancing configuration
 *
 * @author zhouss
 * @since 2022-09-26
 */
@ConfigTypeKey("sermant.springboot.registry.lb")
public class LbConfig implements PluginConfig {
    /**
     * ===============Exclusive configuration for the Zookeeper registry================ The timeout period of the ZK
     * connection
     */
    private int connectionTimeoutMs = LbConstants.DEFAULT_CONNECTION_TIMEOUT_MS;

    /**
     * The timeout period for the ZK response
     */
    private int readTimeoutMs = LbConstants.DEFAULT_READ_TIMEOUT_MS;

    /**
     * ZK connection retry time
     */
    private int retryIntervalMs = LbConstants.DEFAULT_RETRY_INTERVAL_MS;

    /**
     * Zookeeper uri format specification
     */
    private String zkUriSpec = "{scheme}://{address}:{port}";

    /**
     * Server version, the client version needs to match the server version before you can connect
     */
    private String zkServerVersion = "3.4.x";

    /**
     * zookeeper saves the root path of the data, be careful not to match the zk native path (/services), otherwise
     * there may be compatibility issues =============== the end of the Zookeeper registry configuration
     * ================
     */
    private String zkBasePath = "/sermant/services";

    /**
     * Address of the registry
     */
    private String registryAddress = "127.0.0.1:2181";

    /**
     * Registry type, currently only ZK is supported
     */
    private String registryCenterType = "Zookeeper";

    /**
     * When it is enabled, if other registries are the same as the registries of this plugin and are registered to the
     * registry at the same time, the registries that are not registered by the plugin will be automatically excluded
     * from the query
     */
    private boolean onlyCurRegisterInstances = true;

    /**
     * If the registry loses contact and the old instance is used instead of refreshing, note that enabling this option
     * will increase the cache of the old instance in memory
     */
    private boolean keepOldInstancesWhenErr = true;

    /**
     * Whether to enable caching proxy, intercept the class for {@link java.net.HttpURLConnection}, and generate a proxy
     * cache to the map for each host. See the
     * {@link com.huawei.discovery.interceptors.httpconnection.HttpUrlConnectionConnectInterceptor}, turn on this switch
     * to increase memory consumption, but it can avoid frequent creation of proxies
     */
    private boolean enableCacheProxy = false;

    /**
     * When registration fails, the maximum number of retries will be 1 second (registryRetryInterval) to restart the
     * registration each time
     */
    private int registryMaxRetry = LbConstants.DEFAULT_REGISTRY_MAX_RETRY_NUM;

    /**
     * Registration retry wait time
     */
    private long registryRetryInterval = LbConstants.DEFAULT_WAIT_REGISTRY_INTERVAL_MS;

    /**
     * The maximum number of retry configuration caches
     */
    private int maxRetryConfigCache = LbConstants.DEFAULT_MAX_RETRY_CONFIG_CACHE;

    /**
     * The maximum number of retries after the service times out
     */
    private int maxRetry = LbConstants.DEFAULT_MAX_RETRY;

    /**
     * The maximum number of retries for the same instance
     */
    private int maxSameRetry = LbConstants.DEFAULT_MAX_SAME_RETRY;

    /**
     * Retry wait time, default of one second
     */
    private long retryWaitMs = LbConstants.DEFAULT_RETRY_WAIT_MS;

    /**
     * There are two types of retry policies: "RoundRobin" and "Retry the last failed instance
     * (SameInstance){@link LbConfig#maxSameRetry}"
     */
    private String retryPolicy = "RoundRobin";

    /**
     * Retrying scenarios for{@link java.net.SocketTimeoutException}: connect timed out, Do you need to retry? It will
     * be enabled by default
     */
    private boolean enableSocketConnectTimeoutRetry = true;

    /**
     * Retry scenario, against{@link java.net.SocketTimeoutException}: read timed out，Whether you need to try again, it
     * is enabled by default
     */
    private boolean enableSocketReadTimeoutRetry = true;

    /**
     * Retry scenario, against{@link java.util.concurrent.TimeoutException}, Whether you need to retry, it is enabled by
     * default, and this timeout is mostly used in asynchronous scenarios, for example Future, MinimalHttpAsyncClient
     */
    private boolean enableTimeoutExRetry = true;

    /**
     * For additional specified exceptions to retry, please fill in the fully qualified name of the class, currently
     * only JDK exceptions are supported, and whether to trigger the exception depends on the interception point, if the
     * classloader cannot be loaded, it will not take effect
     */
    private List<String> specificExceptionsForRetry = Collections.emptyList();

    /**
     * Instance cache expiration time, if the value is less than 0, it will never expire
     */
    private long instanceCacheExpireTime = LbConstants.DEFAULT_CACHE_EXPIRE_SEC;

    /**
     * If the instance is 0 and the expiration time is not 0, 4/5 of the cache expiration is used as the refresh
     * interval, for example, instanceCacheExpireTime=30S, instanceRefreshInterval=25S
     */
    private long instanceRefreshInterval = 0L;

    /**
     * The timer execution interval in seconds must be less than the instanceCacheExpireTime
     */
    private long refreshTimerInterval = LbConstants.DEFAULT_REFRESH_TIMER_INTERVAL_SEC;

    /**
     * Cache concurrency, which affects the efficiency of getting instances from the cache
     */
    private int cacheConcurrencyLevel = LbConstants.DEFAULT_CACHE_CONCURRENCY_LEVEL;

    /**
     * Service metric data cache, 60 minutes by default
     */
    private long statsCacheExpireTime = LbConstants.DEFAULT_STATS_CACHE_EXPIRE_TIME;

    /**
     * If the refresh time of statistics is set to <=0, the aggregation statistics will not be enabled, and the load
     * balancer associated with the aggregation statistics will become invalid
     */
    private long lbStatsRefreshIntervalMs = LbConstants.DEFAULT_LB_STATS_REFRESH_INTERVAL_MS;

    /**
     * The type of load balancer
     */
    private String lbType = "RoundRobin";

    /**
     * Preference IP, if true, all associated addresses have IP replies host
     */
    private boolean preferIpAddress = false;

    /**
     * The default time window for instance status statistics is 10 minutes, and the statistics will be cleared to 0 at
     * the beginning of each time window
     */
    private long instanceStatTimeWindowMs = LbConstants.DEFAULT_INSTANCE_STATE_TIME_WINDOW_MS;

    public int getRegistryMaxRetry() {
        return registryMaxRetry;
    }

    public boolean isEnableCacheProxy() {
        return enableCacheProxy;
    }

    public void setEnableCacheProxy(boolean enableCacheProxy) {
        this.enableCacheProxy = enableCacheProxy;
    }

    public void setRegistryMaxRetry(int registryMaxRetry) {
        this.registryMaxRetry = registryMaxRetry;
    }

    public long getRegistryRetryInterval() {
        return registryRetryInterval;
    }

    public void setRegistryRetryInterval(long registryRetryInterval) {
        this.registryRetryInterval = registryRetryInterval;
    }

    public String getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(String retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public int getMaxSameRetry() {
        return maxSameRetry;
    }

    public void setMaxSameRetry(int maxSameRetry) {
        this.maxSameRetry = maxSameRetry;
    }

    public boolean isEnableSocketReadTimeoutRetry() {
        return enableSocketReadTimeoutRetry;
    }

    public void setEnableSocketReadTimeoutRetry(boolean enableSocketReadTimeoutRetry) {
        this.enableSocketReadTimeoutRetry = enableSocketReadTimeoutRetry;
    }

    public boolean isEnableTimeoutExRetry() {
        return enableTimeoutExRetry;
    }

    public void setEnableTimeoutExRetry(boolean enableTimeoutExRetry) {
        this.enableTimeoutExRetry = enableTimeoutExRetry;
    }

    public List<String> getSpecificExceptionsForRetry() {
        return specificExceptionsForRetry;
    }

    public void setSpecificExceptionsForRetry(List<String> specificExceptionsForRetry) {
        this.specificExceptionsForRetry = specificExceptionsForRetry;
    }

    public boolean isEnableSocketConnectTimeoutRetry() {
        return enableSocketConnectTimeoutRetry;
    }

    public void setEnableSocketConnectTimeoutRetry(boolean enableSocketConnectTimeoutRetry) {
        this.enableSocketConnectTimeoutRetry = enableSocketConnectTimeoutRetry;
    }

    public long getRefreshTimerInterval() {
        return refreshTimerInterval;
    }

    public void setRefreshTimerInterval(long refreshTimerInterval) {
        this.refreshTimerInterval = refreshTimerInterval;
    }

    public long getInstanceRefreshInterval() {
        return instanceRefreshInterval;
    }

    public void setInstanceRefreshInterval(long instanceRefreshInterval) {
        this.instanceRefreshInterval = instanceRefreshInterval;
    }

    public boolean isOnlyCurRegisterInstances() {
        return onlyCurRegisterInstances;
    }

    public boolean isKeepOldInstancesWhenErr() {
        return keepOldInstancesWhenErr;
    }

    public void setKeepOldInstancesWhenErr(boolean keepOldInstancesWhenErr) {
        this.keepOldInstancesWhenErr = keepOldInstancesWhenErr;
    }

    public void setOnlyCurRegisterInstances(boolean onlyCurRegisterInstances) {
        this.onlyCurRegisterInstances = onlyCurRegisterInstances;
    }

    public String getRegistryCenterType() {
        return registryCenterType;
    }

    public void setRegistryCenterType(String registryCenterType) {
        this.registryCenterType = registryCenterType;
    }

    public String getZkServerVersion() {
        return zkServerVersion;
    }

    public void setZkServerVersion(String version) {
        this.zkServerVersion = version;
    }

    public long getInstanceStatTimeWindowMs() {
        return instanceStatTimeWindowMs;
    }

    public int getMaxRetryConfigCache() {
        return maxRetryConfigCache;
    }

    public void setMaxRetryConfigCache(int maxRetryConfigCache) {
        this.maxRetryConfigCache = maxRetryConfigCache;
    }

    public void setInstanceStatTimeWindowMs(long instanceStatTimeWindowMs) {
        this.instanceStatTimeWindowMs = instanceStatTimeWindowMs;
    }

    public String getZkUriSpec() {
        return zkUriSpec;
    }

    public void setZkUriSpec(String zkUriSpec) {
        this.zkUriSpec = zkUriSpec;
    }

    public long getLbStatsRefreshIntervalMs() {
        return lbStatsRefreshIntervalMs;
    }

    public void setLbStatsRefreshIntervalMs(long lbStatsRefreshIntervalMs) {
        this.lbStatsRefreshIntervalMs = lbStatsRefreshIntervalMs;
    }

    public long getStatsCacheExpireTime() {
        return statsCacheExpireTime;
    }

    public void setStatsCacheExpireTime(long statsCacheExpireTime) {
        this.statsCacheExpireTime = statsCacheExpireTime;
    }

    public int getCacheConcurrencyLevel() {
        return cacheConcurrencyLevel;
    }

    public void setCacheConcurrencyLevel(int cacheConcurrencyLevel) {
        this.cacheConcurrencyLevel = cacheConcurrencyLevel;
    }

    public boolean isPreferIpAddress() {
        return preferIpAddress;
    }

    public void setPreferIpAddress(boolean preferIpAddress) {
        this.preferIpAddress = preferIpAddress;
    }

    public String getLbType() {
        return lbType;
    }

    public void setLbType(String lbType) {
        this.lbType = lbType;
    }

    public long getInstanceCacheExpireTime() {
        return instanceCacheExpireTime;
    }

    public void setInstanceCacheExpireTime(long instanceCacheExpireTime) {
        this.instanceCacheExpireTime = instanceCacheExpireTime;
    }

    public String getZkBasePath() {
        return zkBasePath;
    }

    public void setZkBasePath(String zkBasePath) {
        this.zkBasePath = zkBasePath;
    }

    public int getRetryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(int retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public int getConnectionTimeoutMs() {
        return connectionTimeoutMs;
    }

    public void setConnectionTimeoutMs(int connectionTimeoutMs) {
        this.connectionTimeoutMs = connectionTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public long getRetryWaitMs() {
        return retryWaitMs;
    }

    public void setRetryWaitMs(long retryWaitMs) {
        this.retryWaitMs = retryWaitMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }
}
