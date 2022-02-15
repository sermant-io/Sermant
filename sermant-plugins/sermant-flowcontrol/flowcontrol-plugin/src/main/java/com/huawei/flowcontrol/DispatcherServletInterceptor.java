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

package com.huawei.flowcontrol;

import com.huawei.flowcontrol.common.config.CommonConst;
import com.huawei.flowcontrol.common.entity.FixedResult;
import com.huawei.flowcontrol.common.entity.HttpRequestEntity;
import com.huawei.flowcontrol.common.enums.FlowControlEnum;
import com.huawei.flowcontrol.service.InterceptorSupporter;
import com.huawei.sermant.core.plugin.agent.entity.ExecuteContext;
import com.huawei.sermant.core.plugin.agent.interceptor.Interceptor;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * DispatcherServlet 的 API接口增强 埋点定义sentinel资源
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class DispatcherServletInterceptor extends InterceptorSupporter implements Interceptor {
    /**
     * http请求数据转换 适应plugin -> service数据传递 注意，该方法不可抽出，由于宿主依赖仅可由该拦截器加载，因此抽出会导致找不到类
     *
     * @param request 请求
     * @return HttpRequestEntity
     */
    private HttpRequestEntity convertToHttpEntity(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return new HttpRequestEntity(request.getPathInfo(), request.getServletPath(),
                getHeaders(request), request.getMethod());
    }

    /**
     * 获取http请求头信息
     *
     * @param request 请求信息
     * @return headers
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        final Enumeration<String> headerNames = request.getHeaderNames();
        final Map<String, String> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    @Override
    public ExecuteContext before(ExecuteContext context) throws Exception {
        final Object[] allArguments = context.getArguments();
        final HttpServletRequest argument = (HttpServletRequest) allArguments[0];
        final FixedResult result = new FixedResult();
        chooseHttpService().onBefore(convertToHttpEntity(argument), result);
        if (result.isSkip() && result.getResult() instanceof FlowControlEnum) {
            context.skip(null);
            final HttpServletResponse response = (HttpServletResponse) allArguments[1];
            if (response != null) {
                response.setStatus(CommonConst.TOO_MANY_REQUEST_CODE);
                response.getWriter().print(((FlowControlEnum) result.getResult()).getMsg());
            }
        }
        return context;
    }

    @Override
    public ExecuteContext after(ExecuteContext context) throws Exception {
        chooseHttpService().onAfter(context.getResult());
        return context;
    }

    @Override
    public ExecuteContext onThrow(ExecuteContext context) throws Exception {
        chooseHttpService().onThrow(context.getThrowable());
        return context;
    }
}
