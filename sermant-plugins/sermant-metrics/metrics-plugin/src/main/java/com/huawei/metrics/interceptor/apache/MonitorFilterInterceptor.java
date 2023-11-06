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

package com.huawei.metrics.interceptor.apache;

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.interceptor.AbstractFilterInterceptor;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.RpcContext;

/**
 * dubbo服务监控过滤器增强类
 *
 * @author zhp
 * @since 2023-10-17
 */
public class MonitorFilterInterceptor extends AbstractFilterInterceptor {
    @Override
    public boolean isValid(ExecuteContext context) {
        RpcContext rpcContext = RpcContext.getContext();
        return rpcContext != null && rpcContext.getLocalAddress() != null && rpcContext.getRemoteAddress() != null
                && rpcContext.getUrl() != null;
    }

    @Override
    public MetricsRpcInfo initRpcInfo(ExecuteContext context) {
        RpcContext rpcContext = RpcContext.getContext();
        URL url = rpcContext.getUrl();
        boolean sslEnable = Boolean.parseBoolean(url.getParameter(Constants.SSL_ENABLE));
        return initRpcInfo(rpcContext.getLocalAddress(), rpcContext.getRemoteAddress(),
                url.getParameter(Constants.SIDE_KEY), sslEnable, url.getProtocol());
    }
}
