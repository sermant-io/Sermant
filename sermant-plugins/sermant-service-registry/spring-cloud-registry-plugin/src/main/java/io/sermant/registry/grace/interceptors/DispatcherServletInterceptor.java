/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on org/apache/dubbo/remoting/exchange/support/header/HeaderExchangeServer.java
 * from the Apache Dubbo project.
 */

package io.sermant.registry.grace.interceptors;

import io.sermant.core.plugin.agent.entity.ExecuteContext;
import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.utils.ReflectUtils;
import io.sermant.core.utils.StringUtils;
import io.sermant.registry.config.GraceConfig;
import io.sermant.registry.config.grace.GraceConstants;
import io.sermant.registry.config.grace.GraceContext;
import io.sermant.registry.config.grace.GraceShutDownManager;
import io.sermant.registry.context.RegisterContext;
import io.sermant.registry.context.RegisterContext.ClientInfo;
import io.sermant.registry.services.GraceService;

import java.util.function.BiFunction;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring Web request interceptors
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class DispatcherServletInterceptor extends GraceSwitchInterceptor {
    private final GraceService graceService;

    private final GraceConfig graceConfig;

    private Consumer<Object, String, String> addHeader;

    private Function<Object, Integer> getServerPort;

    private Function<Object, String> getRemoteAddr;

    private BiFunction<Object, String, String> getHeader;

    /**
     * Constructor
     */
    public DispatcherServletInterceptor() {
        graceService = PluginServiceManager.getPluginService(GraceService.class);
        graceConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
        initFunction();
    }

    @Override
    public ExecuteContext doBefore(ExecuteContext context) {
        if (GraceContext.INSTANCE.getStartWarmUpTime() == 0) {
            GraceContext.INSTANCE.setStartWarmUpTime(System.currentTimeMillis());
        }
        Object[] arguments = context.getArguments();
        Object request = arguments[0];
        Object response = arguments[1];
        addGraceAddress(request);
        final GraceShutDownManager graceShutDownManager = GraceContext.INSTANCE.getGraceShutDownManager();
        graceShutDownManager.increaseRequestCount();
        if (graceShutDownManager.isShutDown() && graceConfig.isEnableGraceShutdown()) {
            // It has been marked as closed, and the number of incoming requests has been counted
            final ClientInfo clientInfo = RegisterContext.INSTANCE.getClientInfo();
            addHeader.accept(response, GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT,
                    buildEndpoint(clientInfo.getIp(), clientInfo.getPort()));
            addHeader.accept(response, GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT,
                    buildEndpoint(clientInfo.getHost(), clientInfo.getPort()));
            addHeader.accept(response, GraceConstants.MARK_SHUTDOWN_SERVICE_NAME, clientInfo.getServiceName());
        }
        return context;
    }

    @Override
    public ExecuteContext doAfter(ExecuteContext context) {
        GraceContext.INSTANCE.getGraceShutDownManager().decreaseRequestCount();
        return context;
    }

    @Override
    public ExecuteContext doThrow(ExecuteContext context) {
        GraceContext.INSTANCE.getGraceShutDownManager().decreaseRequestCount();
        return context;
    }

    private void addGraceAddress(Object request) {
        if (graceConfig.isEnableSpring() && graceConfig.isEnableGraceShutdown() && graceConfig.isEnableOfflineNotify()
                && GraceConstants.GRACE_OFFLINE_SOURCE_VALUE
                .equals(getHeader.apply(request, GraceConstants.GRACE_OFFLINE_SOURCE_KEY))) {
            String address = getHeader.apply(request, GraceConstants.SERMANT_GRACE_ADDRESS);
            if (StringUtils.isBlank(address)) {
                address = getRemoteAddr.apply(request) + ":" + getServerPort.apply(request);
            }
            graceService.addAddress(address);
        }
    }

    private void addHeader(Object httpServletResponse, String key, String value) {
        ReflectUtils.invokeMethod(httpServletResponse, "addHeader", new Class[]{String.class, String.class},
                new Object[]{key, value});
    }

    private int getServerPort(Object httpServletRequest) {
        return (int) ReflectUtils.invokeMethodWithNoneParameter(httpServletRequest, "getServerPort").orElse(0);
    }

    private String getRemoteAddr(Object httpServletRequest) {
        return getString(httpServletRequest, "getRemoteAddr");
    }

    private String getHeader(Object httpServletRequest, String key) {
        return (String) ReflectUtils.invokeMethod(httpServletRequest, "getHeader", new Class[]{String.class},
                new Object[]{key}).orElse(null);
    }

    private String getString(Object object, String method) {
        return (String) ReflectUtils.invokeMethodWithNoneParameter(object, method).orElse(null);
    }

    private void initFunction() {
        boolean canLoadLowVersion = canLoadLowVersion();
        if (canLoadLowVersion) {
            addHeader = (obj, key, value) -> ((HttpServletResponse) obj).addHeader(key, value);
            getServerPort = obj -> ((HttpServletRequest) obj).getServerPort();
            getRemoteAddr = obj -> ((HttpServletRequest) obj).getRemoteAddr();
            getHeader = (obj, key) -> ((HttpServletRequest) obj).getHeader(key);
        } else {
            addHeader = this::addHeader;
            getServerPort = this::getServerPort;
            getRemoteAddr = this::getRemoteAddr;
            getHeader = this::getHeader;
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

    /**
     * mapping
     *
     * @param <T> parameter1
     * @param <R> parameter2
     * @param <U> parameter3
     * @author proveceee
     * @since 2024-11-15
     */
    @FunctionalInterface
    private interface Consumer<T, R, U> {
        void accept(T t, R r, U u);
    }
}
