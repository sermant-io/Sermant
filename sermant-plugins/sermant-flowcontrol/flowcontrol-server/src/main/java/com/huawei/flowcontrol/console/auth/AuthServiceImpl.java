/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Based on com/alibaba/csp/sentinel/dashboard/auth/FakeAuthServiceImpl.java from the Alibaba Sentinel project.
 */

package com.huawei.flowcontrol.console.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component
@Primary
@ConditionalOnProperty(name = "auth.enabled", matchIfMissing = true)
public class AuthServiceImpl implements AuthService<HttpServletRequest>, Serializable {
    /**
     * session 主键
     */
    public static final String WEB_SESSION_KEY = "session_sentinel_admin";
    private static final long serialVersionUID = 697982465930182380L;

    @Override
    public AuthUser getAuthUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object sentinelUserObj = session.getAttribute(AuthServiceImpl.WEB_SESSION_KEY);
        if (sentinelUserObj instanceof AuthUser) {
            return (AuthUser) sentinelUserObj;
        }
        return new AuthUserImpl();
    }
}
