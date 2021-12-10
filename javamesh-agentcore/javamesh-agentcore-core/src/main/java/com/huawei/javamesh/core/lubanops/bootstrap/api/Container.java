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

package com.huawei.javamesh.core.lubanops.bootstrap.api;

/**
 * Container. (Singleton, ThreadSafe)
 *
 * @author
 * @date 2020/10/15 20:54
 */
public interface Container {

    /**
     * start method to load the container.
     */
    void start();

    /**
     * stop method to unload the container.
     */
    void stop();

    /**
     * get agent service from container.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T getService(Class<T> clazz);
}
