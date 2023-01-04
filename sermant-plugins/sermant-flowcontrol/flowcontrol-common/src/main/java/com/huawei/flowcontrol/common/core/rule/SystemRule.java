/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.common.core.rule;

import com.huawei.flowcontrol.common.config.CommonConst;

/**
 * 系统规则
 *
 * @author xuezechao1
 * @since 2022-12-05
 */
public class SystemRule extends AbstractRule {
    /**
     * 默认系统负载
     */
    private static final double DEFAULT_SYSTEM_LOAD = Double.MAX_VALUE;

    /**
     * 默认CPU使用率 [0, 1]
     */
    private static final double DEFAULT_CPU_USAGE = 1.0D;

    /**
     * 默认qps
     */
    private static final double DEFAULT_QPS = Double.MAX_VALUE;

    /**
     * 默认平均响应时间
     */
    private static final long DEFAULT_AVE_RT = Long.MAX_VALUE;

    /**
     * 默认并发线程
     */
    private static final long DEFAULT_THREAD_NUM = Long.MAX_VALUE;

    /**
     * 默认限流错误码
     */
    private static final int DEFAULT_ERROR_CODE = CommonConst.INTERVAL_SERVER_ERROR;

    /**
     * 系统负载
     */
    private double systemLoad = DEFAULT_SYSTEM_LOAD;

    /**
     * CPU使用率 [0, 1]
     */
    private double cpuUsage = DEFAULT_CPU_USAGE;

    /**
     * qps
     */
    private double qps = DEFAULT_QPS;

    /**
     * 平均响应时间
     */
    private long aveRt = DEFAULT_AVE_RT;

    /**
     * 并发线程
     */
    private long threadNum = DEFAULT_THREAD_NUM;

    /**
     * 限流错误码
     */
    private int errorCode = DEFAULT_ERROR_CODE;

    @Override
    public boolean isInValid() {
        if (systemLoad < 0) {
            return true;
        }

        if (cpuUsage < 0 || cpuUsage > 1) {
            return true;
        }

        if (qps < 0) {
            return true;
        }

        if (aveRt < 0) {
            return true;
        }

        if (threadNum < 0) {
            return true;
        }
        return super.isInValid();
    }

    public void setSystemLoad(double systemLoad) {
        this.systemLoad = systemLoad;
    }

    public double getSystemLoad() {
        return systemLoad;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }

    public double getQps() {
        return qps;
    }

    public void setAveRt(long aveRt) {
        this.aveRt = aveRt;
    }

    public long getAveRt() {
        return aveRt;
    }

    public void setThreadNum(long threadNum) {
        this.threadNum = threadNum;
    }

    public long getThreadNum() {
        return threadNum;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
