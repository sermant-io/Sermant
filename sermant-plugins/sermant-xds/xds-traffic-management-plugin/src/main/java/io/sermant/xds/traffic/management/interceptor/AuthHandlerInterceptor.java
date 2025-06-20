/*
 * Copyright (C) 2025-2025 Sermant Authors. All rights reserved.
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

package io.sermant.xds.traffic.management.interceptor;

import io.sermant.core.service.ServiceManager;
import io.sermant.core.service.xds.XdsCoreService;
import io.sermant.core.service.xds.XdsSecurityService;
import io.sermant.core.service.xds.entity.XdsSecurityRule;
import io.sermant.xds.traffic.management.authorization.AuthorizationValidator;
import io.sermant.xds.traffic.management.entity.HttpRequestEntity;

import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Jwt HandlerInterceptor,for validate jwt authorization
 *
 * @author lilai
 * @since 2025-06-14
 */
public class AuthHandlerInterceptor implements HandlerInterceptor {
    private XdsSecurityService xdsSecurityService = ServiceManager.getService(XdsCoreService.class)
            .getXdsSecurityService();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) {
        HttpRequestEntity entity = new HttpRequestEntity();
        entity.setHost(request.getHeader(HttpHeaders.HOST));
        entity.setPort(request.getLocalPort());
        entity.setMethod(request.getMethod());
        entity.setPath(request.getRequestURI());
        entity.setParams(AuthorizationValidator.getParams(request));
        entity.setHeaders(AuthorizationValidator.getHeaders(request));
        entity.setDestIp(request.getLocalAddr());
        entity.setSni(request.getServerName());
        entity.setRemoteIp(AuthorizationValidator.getRemoteIpAddress(request));
        entity.setSourceIp(request.getRemoteAddr());

        XdsSecurityRule rule = xdsSecurityService.getXdsSecurityRule();

        boolean idForbidden = AuthorizationValidator.validate(entity, rule);
        if (!idForbidden) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        return idForbidden;
    }
}
