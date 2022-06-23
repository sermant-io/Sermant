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

package com.huawei.registry.config.grace;

/**
 * 优雅上下线上下文
 *
 * @author zhouss
 * @since 2022-05-17
 */
public enum GraceContext {
    /**
     * 单例
     */
    INSTANCE;

    /**
     * 优雅下线管理
     */
    private final GraceShutDownManager graceShutDownManager = new GraceShutDownManager();

    /**
     * 插件开始加载时间
     */
    private long startTime;

    /**
     * 注册完成时间
     */
    private long registryFinishTime;

    /**
     * 第二个注册中心注册完成时间
     */
    private long secondRegistryFinishTime;

    /**
     * 开始预热时间
     */
    private long startWarmUpTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getRegistryFinishTime() {
        return registryFinishTime;
    }

    public void setRegistryFinishTime(long registryFinishTime) {
        this.registryFinishTime = registryFinishTime;
    }

    public GraceShutDownManager getGraceShutDownManager() {
        return graceShutDownManager;
    }

    public void setStartWarmUpTime(long startWarmUpTime) {
        this.startWarmUpTime = startWarmUpTime;
    }

    public long getStartWarmUpTime() {
        return startWarmUpTime;
    }

    public long getSecondRegistryFinishTime() {
        return secondRegistryFinishTime;
    }

    public void setSecondRegistryFinishTime(long secondRegistryFinishTime) {
        this.secondRegistryFinishTime = secondRegistryFinishTime;
    }
}
