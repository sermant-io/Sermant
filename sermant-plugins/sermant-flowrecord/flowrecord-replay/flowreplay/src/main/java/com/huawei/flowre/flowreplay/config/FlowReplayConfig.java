/*
 * Copyright (C) 2021-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowre.flowreplay.config;

/**
 * 压力测试配置 获取回放任务时，从ZK获取更新到本地
 *
 * @author luanwenfei
 * @version 0.0.1
 * @since 2021-07-19
 */
public class FlowReplayConfig {
    private static FlowReplayConfig flowReplayConfig = new FlowReplayConfig();
    /**
     * 回放节点名称
     */
    private String replayWorkerName = Const.BLANK;

    /**
     * 当前回放的JobId
     */
    private String replayJobId = Const.BLANK;

    /**
     * 测试类型 初始化时为功能测试
     */
    private String testType = "functionalTest";

    /**
     * 基线吞吐量
     */
    private int baseLineThroughPut;

    /**
     * 最大并发数量
     */
    private int maxThreadCount;

    /**
     * 最大响应时间
     */
    private int maxResponseTime;

    /**
     * 最低成功率
     */
    private int minSuccessRate;

    private FlowReplayConfig() {
    }

    public static FlowReplayConfig getInstance() {
        return flowReplayConfig;
    }

    public String getReplayWorkerName() {
        return replayWorkerName;
    }

    public void setReplayWorkerName(String replayWorkerName) {
        this.replayWorkerName = replayWorkerName;
    }

    public String getReplayJobId() {
        return replayJobId;
    }

    public void setReplayJobId(String replayJobId) {
        this.replayJobId = replayJobId;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public int getBaseLineThroughPut() {
        return baseLineThroughPut;
    }

    public void setBaseLineThroughPut(int baseLineThroughPut) {
        this.baseLineThroughPut = baseLineThroughPut;
    }

    public int getMaxThreadCount() {
        return maxThreadCount;
    }

    public void setMaxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
    }

    public int getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(int maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }

    public int getMinSuccessRate() {
        return minSuccessRate;
    }

    public void setMinSuccessRate(int minSuccessRate) {
        this.minSuccessRate = minSuccessRate;
    }
}
