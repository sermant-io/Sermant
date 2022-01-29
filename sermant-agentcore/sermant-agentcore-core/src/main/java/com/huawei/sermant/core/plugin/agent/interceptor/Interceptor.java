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

package com.huawei.sermant.core.plugin.agent.interceptor;

import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * 拦截器接口
 *
 * @author HapThorin
 * @version 1.0.0
 * @since 2022-01-24
 */
public interface Interceptor {
    /**
     * 前置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     * @throws Exception 执行异常
     */
    ExecuteContext before(ExecuteContext context) throws Exception;

    /**
     * 后置触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     * @throws Exception 执行异常
     */
    ExecuteContext after(ExecuteContext context) throws Exception;

    /**
     * 异常触发点
     *
     * @param context 执行上下文
     * @return 执行上下文
     * @throws Exception 执行异常
     */
    ExecuteContext onThrow(ExecuteContext context) throws Exception;
}
