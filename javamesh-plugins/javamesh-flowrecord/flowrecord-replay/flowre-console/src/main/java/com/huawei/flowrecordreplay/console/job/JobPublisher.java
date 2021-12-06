/*
 * Copyright (C) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved
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

package com.huawei.flowrecordreplay.console.job;

/**
 *
 * Job configuration center interface
 *
 * @author lilai
 * @param <T>
 * @since 2021-02-26
 */
public interface JobPublisher<T> {
    /**
     * Publish jobs to remote job configuration center
     *
     * @param job job entity to push
     * @throws Exception if some error occurs
     */
    void publish(T job) throws Exception;
}
