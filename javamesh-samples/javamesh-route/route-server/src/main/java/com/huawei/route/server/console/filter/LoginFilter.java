/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.route.server.console.filter;

import com.huawei.route.server.console.auth.AuthServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 文件名：LoginFilter
 * 版权：
 * 描述：过滤器，过滤所有请求
 *
 * @author Gaofang Wu
 * @since 2020-11-15
 * 跟踪单号：
 * 修改单号：
 * 修改内容：session过期返回417状态码，未登录返回401状态码
 */
 @Order(2)
 @Component
public class LoginFilter implements Filter {
    /**
     * 保存cas session的key
     */
    public static final String CONST_CAS_ASSERTION = "_const_cas_assertion_";
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFilter.class);

    /**
     * 是否加载sentinel bean
     */
    @Value("${conditional.cas.load}")
    private boolean isCasLoad;

    /**
     * 自定义的忽略url
     */
    @Value("#{'${ignorePattern}'.replace(',','|')}")
    private String ignorePattern;

    @Value("${conditional.cas.login.skip:false}")
    private boolean skipLogin;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String servletPath = httpRequest.getServletPath();
            if (skipLogin || ignorePattern.contains(servletPath)) {
                chain.doFilter(new SafeHttpServletRequestWrapper((HttpServletRequest) request), response);
                return;
            }
            SafeHttpServletRequestWrapper requestWrapper =
                    new SafeHttpServletRequestWrapper((HttpServletRequest) request);
            if (response instanceof HttpServletResponse) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                HttpSession session = requestWrapper.getSession();

                Object object;
                if (!isCasLoad) {
                    // 获取sentinel用户信息
                    object = session.getAttribute(AuthServiceImpl.WEB_SESSION_KEY);
                } else {
                    // 获取cas用户信息
                    object = session.getAttribute(CONST_CAS_ASSERTION);
                }

                if (object == null) {
                    // If auth fail, set response status code to 401
                    httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                    LOGGER.info("Auth fail, set response status code to 401");
                } else {
                    // 用户信息存在
                    chain.doFilter(requestWrapper, response);
                }
            }
        }
    }

    @Override
    public void destroy() {
    }
}
