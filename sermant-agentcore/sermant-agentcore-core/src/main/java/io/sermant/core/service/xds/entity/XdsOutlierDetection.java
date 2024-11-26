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

package io.sermant.core.service.xds.entity;

/**
 * Xds Outlier Detection information
 *
 * @author zhp
 * @since 2024-11-18
 */
public class XdsOutlierDetection {
    /**
     * Whether to distinguish between local source failures and external errors. When set to true,
     * it will detect local source failures.
     */
    private boolean splitExternalLocalOriginErrors;

    /**
     * The number of consecutive locally originated failures before ejection occurs
     */
    private int consecutiveLocalOriginFailure;

    /**
     * The number of consecutive gateway failures (502, 503, 504 status codes) before a consecutive gateway failure
     * ejection occurs
     */
    private int consecutiveGatewayFailure;

    /**
     * The number of consecutive 5xx responses or local origin errors that are mapped to 5xx error codes before a
     * consecutive 5xx ejection occurs
     */
    private int consecutive5xxFailure;

    /**
     * The time interval between ejection analysis sweeps. This can result in both new ejections as well as hosts being
     * returned to service
     */
    private long interval;

    /**
     * The base time that a host is ejected for. The real time is equal to the base time multiplied by the number of
     * times the host has been ejected
     */
    private long baseEjectionTime;

    /**
     * The maximum % of an upstream cluster that can be ejected due to outlier detection
     */
    private float maxEjectionPercent;

    /**
     * The minimum number of hosts in a cluster in order to perform failure percentage-based ejection
     */
    private float failurePercentageMinimumHosts;

    public boolean isSplitExternalLocalOriginErrors() {
        return splitExternalLocalOriginErrors;
    }

    public void setSplitExternalLocalOriginErrors(boolean splitExternalLocalOriginErrors) {
        this.splitExternalLocalOriginErrors = splitExternalLocalOriginErrors;
    }

    public int getConsecutiveLocalOriginFailure() {
        return consecutiveLocalOriginFailure;
    }

    public void setConsecutiveLocalOriginFailure(int consecutiveLocalOriginFailure) {
        this.consecutiveLocalOriginFailure = consecutiveLocalOriginFailure;
    }

    public int getConsecutiveGatewayFailure() {
        return consecutiveGatewayFailure;
    }

    public void setConsecutiveGatewayFailure(int consecutiveGatewayFailure) {
        this.consecutiveGatewayFailure = consecutiveGatewayFailure;
    }

    public int getConsecutive5xxFailure() {
        return consecutive5xxFailure;
    }

    public void setConsecutive5xxFailure(int consecutive5xxFailure) {
        this.consecutive5xxFailure = consecutive5xxFailure;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getBaseEjectionTime() {
        return baseEjectionTime;
    }

    public void setBaseEjectionTime(long baseEjectionTime) {
        this.baseEjectionTime = baseEjectionTime;
    }

    public float getMaxEjectionPercent() {
        return maxEjectionPercent;
    }

    public void setMaxEjectionPercent(float maxEjectionPercent) {
        this.maxEjectionPercent = maxEjectionPercent;
    }

    public float getFailurePercentageMinimumHosts() {
        return failurePercentageMinimumHosts;
    }

    public void setFailurePercentageMinimumHosts(float failurePercentageMinimumHosts) {
        this.failurePercentageMinimumHosts = failurePercentageMinimumHosts;
    }
}
