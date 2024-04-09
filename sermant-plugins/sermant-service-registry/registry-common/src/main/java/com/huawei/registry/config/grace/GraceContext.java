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

package com.huawei.registry.config.grace;

/**
 * Elegant online and offline context
 *
 * @author zhouss
 * @since 2022-05-17
 */
public enum GraceContext {
    /**
     * Singleton
     */
    INSTANCE;

    /**
     * Elegant offline management
     */
    private final GraceShutDownManager graceShutDownManager = new GraceShutDownManager();

    /**
     * Plugin start load time
     */
    private long startTime;

    /**
     * Registration completion time
     */
    private long registryFinishTime;

    /**
     * The time when the registration of the second registry was completed
     */
    private long secondRegistryFinishTime;

    /**
     * Start warm-up time
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
