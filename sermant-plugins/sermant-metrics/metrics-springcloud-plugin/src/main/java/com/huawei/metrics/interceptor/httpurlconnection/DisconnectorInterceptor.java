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

import com.huawei.metrics.common.Constants;
import com.huawei.metrics.entity.MetricsRpcInfo;
import com.huawei.metrics.interceptor.AbstractHttpInterceptor;
import com.huawei.metrics.manager.MetricsManager;
import com.huawei.metrics.util.ThreadMetricsUtil;

import com.huaweicloud.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huaweicloud.sermant.core.utils.StringUtils;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 连接断开方法增强器
 *
 * @author zhp
 * @since 2023-12-15
 */
public class DisconnectorInterceptor extends AbstractHttpInterceptor {
    @Override
    public ExecuteContext before(ExecuteContext context) {
        return context;
    }

    @Override
    public ExecuteContext collectMetrics(ExecuteContext context) throws Exception {
        if (ThreadMetricsUtil.getStartTime() == null) {
            return context;
        }
        HttpURLConnection httpUrlConnection = (HttpURLConnection) context.getObject();
        URL url = httpUrlConnection.getURL();
        if (url == null) {
            return context;
        }
        boolean enableSsl = StringUtils.equals(url.getProtocol(), Constants.HTTPS_PROTOCOL);
        long latency = System.nanoTime() - (long) ThreadMetricsUtil.getStartTime();
        MetricsRpcInfo metricsRpcInfo = initMetricsInfo(url, enableSsl, latency, httpUrlConnection.getResponseCode());
        MetricsManager.saveRpcInfo(metricsRpcInfo);
        ThreadMetricsUtil.removeStartTime();
        return context;
    }
}
