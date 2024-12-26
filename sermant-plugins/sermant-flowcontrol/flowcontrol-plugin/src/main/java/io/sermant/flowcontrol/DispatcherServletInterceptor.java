/*
 * Copyright (C) 2022-2024 Huawei Technologies Co., Ltd. All rights reserved.
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

package io.sermant.flowcontrol;

import io.sermant.core.exception.SermantRuntimeException;
import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.utils.LogUtils;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.flowcontrol.common.config.ConfigConst;
import io.sermant.flowcontrol.common.entity.FlowControlResult;
import io.sermant.flowcontrol.common.entity.HttpRequestEntity;
import io.sermant.flowcontrol.common.entity.RequestEntity.RequestType;
import io.sermant.flowcontrol.service.InterceptorSupporter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The API interface of DispatcherServlet is enhanced to define sentinel resources.
 *
 * @author zhouss
 * @since 2022-02-11
 */
public class DispatcherServletInterceptor extends InterceptorSupporter {
    private final String className = DispatcherServletInterceptor.class.getName();

    private Function<Object, String> getRequestUri;

    private Function<Object, String> getPathInfo;

    private Function<Object, String> getMethod;

    private Function<Object, Enumeration<String>> getHeaderNames;

    private BiFunction<Object, String, String> getHeader;

    private Function<Object, PrintWriter> getWriter;

    private BiConsumer<Object, Integer> setStatus;

    /**
     * constructor
     */
    public DispatcherServletInterceptor() {
        super();
        initFunction();
    }

    /**
     * http request data conversion adapts to plugin -> service data transfer Note that this method is not
     * extractable，Because host dependencies can only be loaded by this interceptor, pulling out results in classes not
     * being found.
     *
     * @param request request
     * @return HttpRequestEntity
     */
    private Optional<HttpRequestEntity> convertToHttpEntity(Object request) {
        if (request == null) {
            return Optional.empty();
        }
        String uri = getRequestUri.apply(request);
        return Optional.of(new HttpRequestEntity.Builder()
                .setRequestType(RequestType.SERVER)
                .setPathInfo(getPathInfo.apply(request))
                .setServletPath(uri)
                .setHeaders(getHeaders(request))
                .setMethod(getMethod.apply(request))
                .setServiceName(getHeader.apply(request, ConfigConst.FLOW_REMOTE_SERVICE_NAME_HEADER_KEY))
                .build());
    }

    /**
     * gets http request header information
     *
     * @param request request information
     * @return headers
     */
    private Map<String, String> getHeaders(Object request) {
        final Enumeration<String> headerNames = getHeaderNames.apply(request);
        final Map<String, String> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();
            headers.put(headerName, getHeader.apply(request, headerName));
        }
        return Collections.unmodifiableMap(headers);
    }

    @Override
    protected final ExecuteContext doBefore(ExecuteContext context) throws Exception {
        LogUtils.printHttpRequestBeforePoint(context);
        final Object[] allArguments = context.getArguments();
        final Object request = allArguments[0];
        final FlowControlResult result = new FlowControlResult();
        final Optional<HttpRequestEntity> httpRequestEntity = convertToHttpEntity(request);
        if (!httpRequestEntity.isPresent()) {
            return context;
        }
        chooseHttpService().onBefore(className, httpRequestEntity.get(), result);
        if (result.isSkip()) {
            context.skip(null);
            final Object response = allArguments[1];
            if (response != null) {
                setStatus.accept(response, result.getResponse().getCode());
                getWriter.apply(response).print(result.buildResponseMsg());
            }
        }
        return context;
    }

    @Override
    protected final ExecuteContext doAfter(ExecuteContext context) {
        chooseHttpService().onAfter(className, context.getResult());
        LogUtils.printHttpRequestAfterPoint(context);
        return context;
    }

    @Override
    protected final ExecuteContext doThrow(ExecuteContext context) {
        chooseHttpService().onThrow(className, context.getThrowable());
        LogUtils.printHttpRequestOnThrowPoint(context);
        return context;
    }

    private String getRequestUri(Object httpServletRequest) {
        return getString(httpServletRequest, "getRequestURI");
    }

    private String getPathInfo(Object httpServletRequest) {
        return getString(httpServletRequest, "getPathInfo");
    }

    private String getMethod(Object httpServletRequest) {
        return getString(httpServletRequest, "getMethod");
    }

    private Enumeration<String> getHeaderNames(Object httpServletRequest) {
        return (Enumeration<String>) ReflectUtils.invokeMethodWithNoneParameter(httpServletRequest, "getHeaderNames")
                .orElse(null);
    }

    private String getHeader(Object httpServletRequest, String key) {
        return (String) ReflectUtils.invokeMethod(httpServletRequest, "getHeader", new Class[]{String.class},
                new Object[]{key}).orElse(null);
    }

    private PrintWriter getWriter(Object httpServletRequest) {
        return (PrintWriter) ReflectUtils.invokeMethodWithNoneParameter(httpServletRequest, "getWriter")
                .orElse(null);
    }

    private void setStatus(Object httpServletResponse, int code) {
        ReflectUtils.invokeMethod(httpServletResponse, "setStatus", new Class[]{int.class}, new Object[]{code});
    }

    private String getString(Object object, String method) {
        return (String) ReflectUtils.invokeMethodWithNoneParameter(object, method).orElse(null);
    }

    private void initFunction() {
        boolean canLoadLowVersion = canLoadLowVersion();
        if (canLoadLowVersion) {
            getRequestUri = obj -> ((HttpServletRequest) obj).getRequestURI();
            getPathInfo = obj -> ((HttpServletRequest) obj).getPathInfo();
            getMethod = obj -> ((HttpServletRequest) obj).getMethod();
            getHeaderNames = obj -> ((HttpServletRequest) obj).getHeaderNames();
            getHeader = (obj, key) -> ((HttpServletRequest) obj).getHeader(key);
            getWriter = obj -> {
                try {
                    return ((HttpServletResponse) obj).getWriter();
                } catch (IOException ex) {
                    throw new SermantRuntimeException(ex);
                }
            };
            setStatus = (obj, code) -> ((HttpServletResponse) obj).setStatus(code);
        } else {
            getRequestUri = this::getRequestUri;
            getPathInfo = this::getPathInfo;
            getMethod = this::getMethod;
            getHeaderNames = this::getHeaderNames;
            getHeader = this::getHeader;
            getWriter = this::getWriter;
            setStatus = this::setStatus;
        }
    }

    private boolean canLoadLowVersion() {
        try {
            Class.forName(HttpServletRequest.class.getCanonicalName());
        } catch (NoClassDefFoundError | ClassNotFoundException error) {
            return false;
        }
        return true;
    }
}
