/*
 * Copyright (C) 2022-2024 Sermant Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.flowcontrol.res4j.windows;

/**
 * system state
 *
 * @author xuezechao1
 * @since 2022-12-06
 */
public class SystemStatus {
    private static final SystemStatus INSTANCE = new SystemStatus();

    /**
     * current system load
     */
    private double currentLoad = -1;

    /**
     * current system cpu usage
     */
    private double currentCpuUsage = -1;

    /**
     * qps
     */
    private double qps = 0d;

    /**
     * average response time
     */
    private double aveRt = 0d;

    /**
     * minimum response time
     */
    private double minRt = Long.MAX_VALUE;

    /**
     * maximum number of threads
     */
    private long maxThreadNum = Long.MIN_VALUE;

    private SystemStatus() {

    }

    public void setCurrentLoad(double load) {
        this.currentLoad = load;
    }

    public double getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentCpuUsage(double cpuUsage) {
        this.currentCpuUsage = cpuUsage;
    }

    public double getCurrentCpuUsage() {
        return currentCpuUsage;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }

    public double getQps() {
        return qps;
    }

    public void setAveRt(double aveRt) {
        this.aveRt = aveRt;
    }

    public double getAveRt() {
        return aveRt;
    }

    public void setMinRt(double minRt) {
        this.minRt = minRt;
    }

    public double getMinRt() {
        return minRt;
    }

    public void setMaxThreadNum(long threadNum) {
        this.maxThreadNum = threadNum;
    }

    public long getMaxThreadNum() {
        return maxThreadNum;
    }

    public static SystemStatus getInstance() {
        return INSTANCE;
    }
}
