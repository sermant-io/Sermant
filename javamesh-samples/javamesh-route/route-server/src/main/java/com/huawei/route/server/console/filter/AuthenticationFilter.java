/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.route.server.console.filter;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 无损演练标签权限控制
 *
 * @author qinfurong
 * @since 2021-08-12
 */
//@SuppressWarnings("ALL")
//@WebFilter(urlPatterns = {"/label/add", "/label/update", "/label/isValid", "/label/delete", "/label/business"})
public class AuthenticationFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            // 判断无损演练标签的权限
            String labelName = getLabelName(httpRequest);
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }

    private String getLabelName(HttpServletRequest httpRequest) throws IOException {
        try (BufferedReader streamReader = new BufferedReader(new InputStreamReader(httpRequest.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            Map body = JSON.parseObject(responseStrBuilder.toString(), Map.class);
            if (body != null) {
                return String.valueOf(body.get("labelName"));
            }
            return httpRequest.getParameter("labelName");
        } catch (IOException exception) {
            throw exception;
        }
    }
}
