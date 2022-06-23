/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.registry.config;

import com.huawei.registry.config.grace.GraceConstants;

import com.huaweicloud.sermant.core.common.LoggerFactory;
import com.huaweicloud.sermant.core.config.common.ConfigTypeKey;
import com.huaweicloud.sermant.core.plugin.config.PluginConfig;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * 优雅上下线相关配置
 *
 * @author zhouss
 * @since 2022-05-17
 */
@ConfigTypeKey(value = "grace.rule")
public class GraceConfig implements PluginConfig, Cloneable {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * 是否开启优雅上下线
     */
    private boolean enableSpring = false;

    /**
     * 启动延迟时间, 单位秒
     */
    private long startDelayTime = 0L;

    /**
     * 是否开启服务预热
     */
    private boolean enableWarmUp = false;

    /**
     * 预热时间
     */
    private long warmUpTime = 0L;

    /**
     * 预热权重, 默认100
     */
    private int warmUpWeight = GraceConstants.DEFAULT_WARM_UP_WEIGHT;

    /**
     * 默认计算曲线值
     */
    private int warmUpCurve = GraceConstants.DEFAULT_WARM_UP_CURVE;

    /**
     * 是否开启优雅关闭
     */
    private boolean enableGraceShutdown = false;

    /**
     * 关闭等待时间, 单位秒
     */
    private long shutdownWaitTime = 0L;

    /**
     * 关闭前自动检测请求数以及关联的请求的地址是否都已处理完毕, 确保流量不丢失, 单位秒 每shutdownCheckTimeUnit检测一次,直到达到shutdownWaitTime或者处理完
     */
    private long shutdownCheckTimeUnit = 1L;

    /**
     * 是否开启下线主动通知
     */
    private boolean enableOfflineNotify;

    /**
     * 开启下线主动通知时的httpServer端口
     */
    private int httpServerPort = GraceConstants.DEFAULT_NOTIFY_HTTP_SERVER_PORT;

    /**
     * 下游Endpoint过期时间, 该配置关联注册中心的最大缓存时间, 建议过期时间大于注册中心自身实例缓存时间, 默认120S
     */
    private long endpointExpiredTime = GraceConstants.DEFAULT_ENDPOINT_EXPIRED_TIME;

    /**
     * 优雅上下线聚合开关
     */
    private boolean enableGrace = false;

    /**
     * 缓存上游地址的默认大小
     */
    private long upstreamAddressMaxSize = GraceConstants.UPSTREAM_ADDRESS_DEFAULT_MAX_SIZE;

    /**
     * 缓存上游地址的过期时间
     */
    private long upstreamAddressExpiredTime = GraceConstants.UPSTREAM_ADDRESS_DEFAULT_EXPIRED_TIME;

    /**
     * 根据聚合开关修正相关开关属性, 一键开启优雅上下线所有功能
     */
    public void fixGraceSwitch() {
        this.enableGrace = Boolean.parseBoolean(getConfigFromEnv(GraceConstants.ENV_GRACE_ENABLE, null));
        if (enableGrace) {
            this.enableGraceShutdown = true;
            this.enableOfflineNotify = true;
            this.enableWarmUp = true;
        }
    }

    /**
     * 从环境变量获取配置
     *
     * @param configKey 键
     * @param defaultValue 默认值
     * @return 环境变量配置
     */
    private String getConfigFromEnv(String configKey, String defaultValue) {
        String property = System.getProperty(configKey);
        if (property == null) {
            property = System.getenv(configKey);
        }
        return property == null ? defaultValue : property;
    }

    public boolean isEnableGrace() {
        return enableGrace;
    }

    public void setEnableGrace(boolean enableGrace) {
        this.enableGrace = enableGrace;
    }

    public long getEndpointExpiredTime() {
        return endpointExpiredTime;
    }

    public void setEndpointExpiredTime(long endpointExpiredTime) {
        this.endpointExpiredTime = endpointExpiredTime;
    }

    public boolean isEnableGraceShutdown() {
        return enableGraceShutdown;
    }

    public void setEnableGraceShutdown(boolean enableGraceShutdown) {
        this.enableGraceShutdown = enableGraceShutdown;
    }

    public long getShutdownWaitTime() {
        return shutdownWaitTime;
    }

    public void setShutdownWaitTime(long shutdownWaitTime) {
        this.shutdownWaitTime = shutdownWaitTime;
    }

    public long getShutdownCheckTimeUnit() {
        return shutdownCheckTimeUnit;
    }

    public void setShutdownCheckTimeUnit(long shutdownCheckTimeUnit) {
        this.shutdownCheckTimeUnit = shutdownCheckTimeUnit;
    }

    public int getWarmUpWeight() {
        return warmUpWeight;
    }

    public void setWarmUpWeight(int warmUpWeight) {
        this.warmUpWeight = warmUpWeight;
    }

    public int getWarmUpCurve() {
        return warmUpCurve;
    }

    public void setWarmUpCurve(int warmUpCurve) {
        this.warmUpCurve = warmUpCurve;
    }

    public boolean isEnableWarmUp() {
        return enableWarmUp;
    }

    public void setEnableWarmUp(boolean enableWarmUp) {
        this.enableWarmUp = enableWarmUp;
    }

    public long getWarmUpTime() {
        return warmUpTime;
    }

    public void setWarmUpTime(long warmUpTime) {
        this.warmUpTime = warmUpTime;
    }

    public boolean isEnableSpring() {
        return enableSpring;
    }

    public void setEnableSpring(boolean enableSpring) {
        this.enableSpring = enableSpring;
    }

    public long getStartDelayTime() {
        return startDelayTime;
    }

    public void setStartDelayTime(long startDelayTime) {
        this.startDelayTime = startDelayTime;
    }

    public boolean isEnableOfflineNotify() {
        return enableOfflineNotify;
    }

    public void setEnableOfflineNotify(boolean enableOfflineNotify) {
        this.enableOfflineNotify = enableOfflineNotify;
    }

    public int getHttpServerPort() {
        return httpServerPort;
    }

    public void setHttpServerPort(int httpServerPort) {
        this.httpServerPort = httpServerPort;
    }

    public long getUpstreamAddressMaxSize() {
        return upstreamAddressMaxSize;
    }

    public void setUpstreamAddressMaxSize(long upstreamAddressMaxSize) {
        this.upstreamAddressMaxSize = upstreamAddressMaxSize;
    }

    public long getUpstreamAddressExpiredTime() {
        return upstreamAddressExpiredTime;
    }

    public void setUpstreamAddressExpiredTime(long upstreamAddressExpiredTime) {
        this.upstreamAddressExpiredTime = upstreamAddressExpiredTime;
    }

    /**
     * 预热参数是否合法
     *
     * @return 合法返回true
     */
    public boolean isWarmUpValid() {
        if (warmUpTime < 0) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Invalid warmUpTime [%s]", warmUpTime));
            return false;
        }
        if (warmUpCurve < 1) {
            LOGGER.warning(String.format(Locale.ENGLISH, "Param warmUpCurve must ga 1, but now is [%s]", warmUpCurve));
            return false;
        }
        return true;
    }

    @Override
    public GraceConfig clone() {
        try {
            return (GraceConfig) super.clone();
        } catch (CloneNotSupportedException ex) {
            LoggerFactory.getLogger().warning("Can not clone class GraceConfig");
            throw new IllegalStateException(
                    "Got a CloneNotSupportedException from Object.clone() even though we're Cloneable!");
        }
    }

    @Override
    public String toString() {
        return "GraceConfig{"
                + "enableSpring=" + enableSpring
                + ", startDelayTime=" + startDelayTime
                + ", enableWarmUp=" + enableWarmUp
                + ", warmUpTime=" + warmUpTime
                + ", warmUpWeight=" + warmUpWeight
                + ", warmUpCurve=" + warmUpCurve
                + ", enableGraceShutdown=" + enableGraceShutdown
                + ", shutdownWaitTime=" + shutdownWaitTime
                + ", shutdownCheckTimeUnit=" + shutdownCheckTimeUnit
                + ", enableOfflineNotify=" + enableOfflineNotify
                + ", httpServerPort=" + httpServerPort
                + ", endpointExpiredTime=" + endpointExpiredTime
                + ", enableGrace=" + enableGrace
                + ", upstreamAddressMaxSize=" + upstreamAddressMaxSize
                + ", upstreamAddressExpiredTime=" + upstreamAddressExpiredTime
                + '}';
    }
}
