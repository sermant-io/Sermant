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

package com.huawei.sermant.core.service;

/**
 * Agent服务接口
 * <p>
 * 实现{@link BaseService}的服务实例会在agent启动时被创建，
 * 并在创建完成之后，依次调用的它们的{@link #start()}方法，
 * 同时为他们创建钩子，用于在JVM结束时调用服务是{@link #stop()}方法
 * <p>
 * 所有{@link BaseService}实例将由{@link ServiceManager}管理，
 * 可通过{@link ServiceManager#getService(Class)}方法传入{@link BaseService}子类获取服务实例。
 * {@link ServiceManager}将根据传入的{@link BaseService}类型，返回最佳的实现类。
 * 可以通过{@link com.huawei.sermant.core.utils.SpiLoadUtils.SpiWeight}注解设置多实现类的权重。
 *
 * @author justforstudy-A, beetle-man, HapThorin
 * @version 1.0.0
 * @since 2022-01-21
 */
public interface BaseService {

    /**
     * 服务启动方法
     */
    default void start() {
    }

    /**
     * 服务关闭方法
     */
    default void stop() {
    }
}
