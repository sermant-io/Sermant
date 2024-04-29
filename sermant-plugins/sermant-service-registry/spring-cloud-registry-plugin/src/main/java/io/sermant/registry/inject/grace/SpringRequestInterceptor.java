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

package io.sermant.registry.inject.grace;

import io.sermant.core.plugin.config.PluginConfigManager;
import io.sermant.core.plugin.service.PluginServiceManager;
import io.sermant.core.utils.StringUtils;
import io.sermant.registry.config.GraceConfig;
import io.sermant.registry.config.grace.GraceConstants;
import io.sermant.registry.config.grace.GraceContext;
import io.sermant.registry.config.grace.GraceShutDownManager;
import io.sermant.registry.context.RegisterContext;
import io.sermant.registry.context.RegisterContext.ClientInfo;
import io.sermant.registry.grace.interceptors.SpringWebHandlerInterceptor;
import io.sermant.registry.services.GraceService;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Spring Web request pre-interceptors, dynamically joined by interceptors, are added with each request,
 * {@link SpringWebHandlerInterceptor}
 *
 * @author zhouss
 * @since 2022-05-23
 */
public class SpringRequestInterceptor implements HandlerInterceptor {
    private final GraceService graceService;

    private final GraceConfig graceConfig;

    /**
     * Constructor
     */
    public SpringRequestInterceptor() {
        graceService = PluginServiceManager.getPluginService(GraceService.class);
        graceConfig = PluginConfigManager.getPluginConfig(GraceConfig.class);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (GraceContext.INSTANCE.getStartWarmUpTime() == 0) {
            GraceContext.INSTANCE.setStartWarmUpTime(System.currentTimeMillis());
        }
        addGraceAddress(request);
        final GraceShutDownManager graceShutDownManager = GraceContext.INSTANCE.getGraceShutDownManager();
        graceShutDownManager.increaseRequestCount();
        if (graceShutDownManager.isShutDown() && graceConfig.isEnableGraceShutdown()) {
            // It has been marked as closed, and the number of incoming requests has been counted
            final ClientInfo clientInfo = RegisterContext.INSTANCE.getClientInfo();
            response.addHeader(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT,
                    buildEndpoint(clientInfo.getIp(), clientInfo.getPort()));
            response.addHeader(GraceConstants.MARK_SHUTDOWN_SERVICE_ENDPOINT,
                    buildEndpoint(clientInfo.getHost(), clientInfo.getPort()));
            response.addHeader(GraceConstants.MARK_SHUTDOWN_SERVICE_NAME, clientInfo.getServiceName());
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        GraceContext.INSTANCE.getGraceShutDownManager().decreaseRequestCount();
    }

    private String buildEndpoint(String host, int port) {
        return host + ":" + port;
    }

    private void addGraceAddress(HttpServletRequest request) {
        if (graceConfig.isEnableSpring() && graceConfig.isEnableGraceShutdown() && graceConfig.isEnableOfflineNotify()
                && GraceConstants.GRACE_OFFLINE_SOURCE_VALUE
                .equals(request.getHeader(GraceConstants.GRACE_OFFLINE_SOURCE_KEY))) {
            String address = request.getHeader(GraceConstants.SERMANT_GRACE_ADDRESS);
            if (StringUtils.isBlank(address)) {
                address = request.getRemoteAddr() + ":" + request.getServerPort();
            }
            graceService.addAddress(address);
        }
    }
}
