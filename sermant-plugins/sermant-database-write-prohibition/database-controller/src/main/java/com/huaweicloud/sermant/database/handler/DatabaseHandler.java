/*
 *  Copyright (C) 2024-2024 Huawei Technologies Co., Ltd. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.huaweicloud.sermant.database.handler;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

/**
 * 数据库禁写处理接口
 *
 * @author daizhenyu
 * @since 2024-01-15
 **/
public interface DatabaseHandler {
    /**
     * 拦截点前置处理
     *
     * @param context 上下文信息
     */
    void doBefore(ExecuteContext context);

    /**
     * 拦截点后置处理
     *
     * @param context 上下文信息
     */
    void doAfter(ExecuteContext context);

    /**
     * 拦截点异常处理
     *
     * @param context 上下文信息
     */
    void doOnThrow(ExecuteContext context);
}
