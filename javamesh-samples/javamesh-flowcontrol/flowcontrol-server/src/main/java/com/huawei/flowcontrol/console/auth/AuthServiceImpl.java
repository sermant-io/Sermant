
/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.flowcontrol.console.auth;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

/**
 * 文件名：AuthServiceImpl
 * 版权：
 * 描述：用户接口实现类
 *
 * @author Gaofang Wu
 * @since 2020-11-25
 * 跟踪单号：
 * 修改单号：
 * 修改内容：序列化
 */
@Component
@Primary
@ConditionalOnProperty(name = "auth.enabled", matchIfMissing = true)
public class AuthServiceImpl implements AuthService<HttpServletRequest>, Serializable {
    /**
     * session 主键
     */
    public static final String WEB_SESSION_KEY = "session_sentinel_admin";

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
