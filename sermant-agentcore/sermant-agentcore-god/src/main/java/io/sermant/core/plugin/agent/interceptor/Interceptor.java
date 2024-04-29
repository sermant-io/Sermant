/*
 * Copyright (C) 2021-2022 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.core.plugin.agent.interceptor;

import io.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * Interceptor interface
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public interface Interceptor {
    /**
     * Pre-trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
     */
    ExecuteContext before(ExecuteContext context) throws Exception;

    /**
     * Post trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
     */
    ExecuteContext after(ExecuteContext context) throws Exception;

    /**
     * Exception trigger point
     *
     * @param context Execution context
     * @return Execution context
     * @throws Exception Execution exception
     */
    ExecuteContext onThrow(ExecuteContext context) throws Exception;
}
