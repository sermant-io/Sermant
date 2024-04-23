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

package io.sermant.registry.config;

import io.sermant.core.common.LoggerFactory;
import io.sermant.core.config.common.ConfigTypeKey;
import io.sermant.core.plugin.config.PluginConfig;
import io.sermant.registry.config.grace.GraceConstants;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Elegant online and offline related configurations
 *
 * @author zhouss
 * @since 2022-05-17
 */
@ConfigTypeKey(value = "grace.rule")
public class GraceConfig implements PluginConfig, Cloneable {
    private static final Logger LOGGER = LoggerFactory.getLogger();

    /**
     * Whether to turn on the elegant online and offline lines
     */
    private boolean enableSpring = false;

    /**
     * Startup delay time, in seconds
     */
    private long startDelayTime = 0L;

    /**
     * Specifies whether to enable service warm-up
     */
    private boolean enableWarmUp = false;

    /**
     * Warm-up time
     */
    private long warmUpTime = 0L;

    /**
     * The warm-up weight is 100 by default
     */
    private int warmUpWeight = GraceConstants.DEFAULT_WARM_UP_WEIGHT;

    /**
     * The curve value is calculated by default
     */
    private int warmUpCurve = GraceConstants.DEFAULT_WARM_UP_CURVE;

    /**
     * Whether to turn on graceful closing
     */
    private boolean enableGraceShutdown = false;

    /**
     * Shutdown wait time, in seconds
     */
    private long shutdownWaitTime = 0L;

    /**
     * Automatically detect whether the number of requests and the associated request addresses have been processed
     * before shutdown to ensure that traffic is not lost.
     * Unit: The detection is performed every shutdownCheckTimeUnit, which unit is second
     * until the shutdownWaitTime is reached or the processing is completed
     */
    private long shutdownCheckTimeUnit = 1L;

    /**
     * Specifies whether to enable offline notifications
     */
    private boolean enableOfflineNotify;

    /**
     * Enable the HTTP server port for active notifications when you go offline
     */
    private int httpServerPort = GraceConstants.DEFAULT_NOTIFY_HTTP_SERVER_PORT;

    /**
     * Downstream endpoint expiration time, the maximum cache time of the associated registry, the recommended
     * expiration time is greater than the cache time of the registry itself, which is 120 seconds by default
     */
    private long endpointExpiredTime = GraceConstants.DEFAULT_ENDPOINT_EXPIRED_TIME;

    /**
     * Elegant on-line and off-line polymerization switches
     */
    private boolean enableGrace = false;

    /**
     * The default size of the cache upstream address
     */
    private long upstreamAddressMaxSize = GraceConstants.UPSTREAM_ADDRESS_DEFAULT_MAX_SIZE;

    /**
     * Cache the expiration time of upstream addresses
     */
    private long upstreamAddressExpiredTime = GraceConstants.UPSTREAM_ADDRESS_DEFAULT_EXPIRED_TIME;

    /**
     * Correct the relevant switch attributes according to the aggregation switch,
     * and turn on all functions of elegant online and offline with one click
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
     * Get the configuration from the environment variables
     *
     * @param configKey Key
     * @param defaultValue Default value
     * @return Environment variable configuration
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
     * Check whether the preheating parameter is valid
     *
     * @return Valid returns true
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
        if (httpServerPort <= 0 || httpServerPort > GraceConstants.MAX_HTTP_SERVER_PORT) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Param httpServerPort must at interval 0-65535, but now is [%s]", httpServerPort));
            return false;
        }
        if (shutdownWaitTime < 0 || shutdownWaitTime > GraceConstants.MAX_SHUTDOWN_WAIT_TIME) {
            LOGGER.warning(String.format(Locale.ENGLISH,
                    "Param shutdownWaitTime must at interval 0-24h, but now is [%s S]", shutdownWaitTime));
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
