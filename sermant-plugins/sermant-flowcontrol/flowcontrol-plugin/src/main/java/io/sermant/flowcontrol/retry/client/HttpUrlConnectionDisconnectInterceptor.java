/*
 * Copyright (C) 2024-2024 Sermant Authors. All rights reserved.
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

package io.sermant.flowcontrol.retry.client;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.flowcontrol.AbstractXdsHttpClientInterceptor;
import io.sermant.flowcontrol.common.config.CommonConst;
import io.sermant.flowcontrol.common.util.XdsThreadLocalUtil;

import java.lang.reflect.Method;
import java.net.HttpURLConnection;

/**
 * This intercept point mainly cleans up the connection at the final stage
 *
 * @author zhouss
 * @since 2024-12-20
 */
public class HttpUrlConnectionDisconnectInterceptor extends AbstractXdsHttpClientInterceptor {
    private static final String STATUS_CODE = "status_code";

    /**
     * Constructor
     */
    public HttpUrlConnectionDisconnectInterceptor() {
        super(null, HttpUrlConnectionDisconnectInterceptor.class.getCanonicalName());
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) throws Exception {
        HttpURLConnection httpUrlConnection = (HttpURLConnection) context.getObject();
        context.setLocalFieldValue(STATUS_CODE, httpUrlConnection.getResponseCode());
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        XdsThreadLocalUtil.removeConnectionStatus();
        XdsThreadLocalUtil.removeHttpUrlConnection();
        super.doAfter(context);
        return context;
    }

    @Override
    public ExecuteContext doThrow(ExecuteContext context) {
        if (context.getThrowableOut() != null) {
            XdsThreadLocalUtil.removeConnectionStatus();
            XdsThreadLocalUtil.removeHttpUrlConnection();
        }
        super.doThrow(context);
        return context;
    }

    @Override
    protected int getStatusCode(ExecuteContext context) {
        Object statusCode = context.getLocalFieldValue(STATUS_CODE);
        if (statusCode instanceof Integer) {
            return (int) statusCode;
        }
        return CommonConst.DEFAULT_RESPONSE_CODE;
    }

    @Override
    protected void preRetry(Object obj, Method method, Object[] allArguments, Object result, boolean isFirstInvoke) {
    }

    @Override
    protected boolean canInvoke(ExecuteContext context) {
        return true;
    }
}
