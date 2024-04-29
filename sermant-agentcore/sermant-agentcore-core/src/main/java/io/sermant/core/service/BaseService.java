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

package io.sermant.core.service;

/**
 * Agent service interface
 * <p>
 * Service instances implementing {@link BaseService} is created when the agent starts, and after creating them, call
 * their {@link #start()} method in turn,
 * We also create hooks for them to invoke the {@link #stop()} method of the services at the end of the JVM
 * <p>
 * All {@link BaseService} instances will be managed by {@link ServiceManager}，
 * You can get the service instance by inputting the {@link BaseService} subclass through the
 * {@link ServiceManager#getService(Class)} method.
 * {@link ServiceManager} returns the best implementation class based on the {@link BaseService} type input. You can set
 * the weight of multiple implementation classes with the SpiLoadUtils.SpiWeight annotation.
 *
 * @author justforstudy-A, beetle-man, HapThorin
 * @version 1.0.0
 * @since 2022-01-21
 */
public interface BaseService {
    /**
     * Service start method
     */
    default void start() {
    }

    /**
     * Service stop method
     */
    default void stop() {
    }
}
