/*
 * Copyright (C) 2023-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.metrics.interceptor.httpurlconnection;

import com.huawei.metrics.util.ThreadMetricsUtil;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.plugin.agent.interceptor.Interceptor;

/**
 * HttpUrlConnection连接方法增强器
 *
 * @author zhp
 * @since 2023-12-15
 */
public class ConnectorInterceptor implements Interceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        // 设置请求开始时间，链接断开时用于计算时延
        ThreadMetricsUtil.setStartTime(System.nanoTime());
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) {
        return context;
    }
}
