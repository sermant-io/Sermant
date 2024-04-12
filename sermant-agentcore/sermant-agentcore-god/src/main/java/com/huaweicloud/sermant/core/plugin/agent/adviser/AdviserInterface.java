/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huaweicloud.sermant.core.plugin.agent.adviser;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * Adviser interface
 *
 * @author luanwenfei
 * @since 2023-04-11
 */
public interface AdviserInterface {
    /**
     * The preceding trigger point of method
     *
     * @param context execute Context
     * @param adviceKey enhanced class name
     * @return ExecuteContext
     * @throws Throwable Throwable
     */
    ExecuteContext onMethodEnter(ExecuteContext context, String adviceKey) throws Throwable;

    /**
     * The post trigger point of method
     *
     * @param context execute Context
     * @param adviceKey enhanced class name
     * @return ExecuteContext
     * @throws Throwable Throwable
     */
    ExecuteContext onMethodExit(ExecuteContext context, String adviceKey) throws Throwable;
}
