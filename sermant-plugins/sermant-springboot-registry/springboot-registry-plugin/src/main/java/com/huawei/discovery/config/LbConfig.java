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
 * 负载均衡配置
 *
 * @author zhouss
 * @since 2022-09-26
 */
@ConfigTypeKey("sermant.springboot.registry.lb")
public class LbConfig implements PluginConfig {
    /**
     * ===============Zookeeper注册中心专属配置================
     * ZK连接超时时间
     */
    private int connectionTimeoutMs = LbConstants.DEFAULT_CONNECTION_TIMEOUT_MS;

    /**
     * ZK响应超时时间
     */
    private int readTimeoutMs = LbConstants.DEFAULT_READ_TIMEOUT_MS;

    /**
     * ZK连接重试时间
     */
    private int retryIntervalMs = LbConstants.DEFAULT_RETRY_INTERVAL_MS;

    /**
     * zookeeper uri格式规范
     */
    private String zkUriSpec = "{scheme}://{address}:{port}";

    /**
     * 服务端版本, 客户端版本需要与服务端版本相匹配才可连接
     */
    private String zkServerVersion = "3.4.x";

    /**
     * zookeeper保存数据的根路径, 注意不要与zk原生路径一致(/services), 否则可能存在兼容性问题
     * ===============Zookeeper注册中心专属配置结束================
     */
    private String zkBasePath = "/sermant/services";

    /**
     * 注册中心地址
     */
    private String registryAddress = "127.0.0.1:2181";

    /**
     * 注册中心类型, 当前仅支持ZK
     */
    private String registryCenterType = "Zookeeper";

    /**
     * 是否仅仅查询本插件注册的实例, 开启时, 如果其他注册中心与本插件注册中心一致, 同时注册到注册中心, 查询时会自动排除非本插件注册的注册中心
     */
    private boolean onlyCurRegisterInstances = true;

    /**
     * 若注册中心失联，是否使用旧的实例而不会刷新, 注意, 开启此选项内存中将会增加旧实例的缓存
     */
    private boolean keepOldInstancesWhenErr = true;

    /**
     * 注册失败时, 最大重试次数, 每次会等待1秒(registryRetryInterval)重新发起注册
     */
    private int registryMaxRetry = LbConstants.DEFAULT_REGISTRY_MAX_RETRY_NUM;

    /**
     * 注册重试等待时间
     */
    private long registryRetryInterval = LbConstants.DEFAULT_WAIT_REGISTRY_INTERVAL_MS;

    /**
     * 最大的重试配置缓存数
     */
    private int maxRetryConfigCache = LbConstants.DEFAULT_MAX_RETRY_CONFIG_CACHE;

    /**
     * 服务超时后最大重试次数
     */
    private int maxRetry = LbConstants.DEFAULT_MAX_RETRY;

    /**
     * 最大相同实例的重试次数
     */
    private int maxSameRetry = LbConstants.DEFAULT_MAX_SAME_RETRY;

    /**
     * 重试等待时间, 默认一秒
     */
    private long retryWaitMs = LbConstants.DEFAULT_RETRY_WAIT_MS;

    /**
     * 重试策略, 当前支持两种"轮询(RoundRobin)"与"先重试上一次失败的实例(SameInstance){@link LbConfig#maxSameRetry}"
     */
    private String retryPolicy = "RoundRobin";

    /**
     * 重试场景, 针对{@link java.net.SocketTimeoutException}: connect timed out是否需要重试, 默认开启
     */
    private boolean enableSocketConnectTimeoutRetry = true;

    /**
     * 重试场景, 针对{@link java.net.SocketTimeoutException}: read timed out是否需要重试, 默认开启
     */
    private boolean enableSocketReadTimeoutRetry = true;

    /**
     * 重试场景, 针对{@link java.util.concurrent.TimeoutException}, 是否需要重试, 默认开启, 该超时多用于异步场景, 例如Future, MinimalHttpAsyncClient
     */
    private boolean enableTimeoutExRetry = true;

    /**
     * 针对额外指定异常进行重试, 请填写类的全限定名, 目前仅支持jdk异常, 且是否触发该异常由拦截点而定, 若类加载器无法加载则无法生效
     */
    private List<String> specificExceptionsForRetry = Collections.emptyList();

    /**
     * 实例缓存过期时间, 若该值小于0, 则永远不会过期
     */
    private long instanceCacheExpireTime = LbConstants.DEFAULT_CACHE_EXPIRE_SEC;

    /**
     * 实例全量刷新间隔, 单位秒, 若为0, 且过期时间不为0, 则会取缓存过期的4/5作为刷新间隔；例如instanceCacheExpireTime=30S, 则instanceRefreshInterval=25S
     */
    private long instanceRefreshInterval = 0L;

    /**
     * 定时器执行间隔, 单位秒, 一定要小于instanceCacheExpireTime
     */
    private long refreshTimerInterval = LbConstants.DEFAULT_REFRESH_TIMER_INTERVAL_SEC;

    /**
     * 缓存并发度, 影响从缓存获取实例的效率
     */
    private int cacheConcurrencyLevel = LbConstants.DEFAULT_CACHE_CONCURRENCY_LEVEL;

    /**
     * 服务指标数据缓存, 默认60分钟
     */
    private long statsCacheExpireTime = LbConstants.DEFAULT_STATS_CACHE_EXPIRE_TIME;

    /**
     * 统计数据定时聚合统计刷新时间, 若设置<=0, 则不会开启聚合统计, 关联聚合统计的负载均衡将会失效
     */
    private long lbStatsRefreshIntervalMs = LbConstants.DEFAULT_LB_STATS_REFRESH_INTERVAL_MS;

    /**
     * 负载均衡类型
     */
    private String lbType = "RoundRobin";

    /**
     * 倾向IP, 若为true, 则所有关联的地址均有ip替换host
     */
    private boolean preferIpAddress = false;

    /**
     * 实例状态统计时间窗口, 默认10分钟, 每一个时间窗口的开始, 统计都会清0
     */
    private long instanceStatTimeWindowMs = LbConstants.DEFAULT_INSTANCE_STATE_TIME_WINDOW_MS;

    public int getRegistryMaxRetry() {
        return registryMaxRetry;
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
