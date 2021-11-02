/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2021-2021. All rights reserved.
 */

package com.huawei.route.server.console.filter;

import com.alibaba.fastjson.JSONObject;
import com.huawei.route.common.Result;
import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.http.HttpStatus;
import redis.clients.jedis.exceptions.JedisException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 功能描述：filter部分全局异常拦截
 *
 * @author z30009938
 * @since 2021-09-26
 */
//@Order(1)
//@WebFilter
//@Component
public class GlobalExceptionFilter implements Filter {
    /**
     * 日志工具
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionFilter.class);

    /**
     * filter报错的时候错误消息内容响应类型
     */
    private static final String RESPONSE_CONTENT_TYPE = "application/json;charset=UTF-8";

    /**
     * filter报错的时候错误相应编码
     */
    private static final String RESPONSE_CHAR_SET = "UTF-8";

    /**
     * filter中redis导致的错误类型
     */
    private static final String REDIS_ERROR_TYPE = "redis";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (QueryTimeoutException | RedisException | RedisSystemException | JedisException redisException) {
            LOGGER.error("Global exception in filters." + redisException);
            responseErrorMsg(httpResponse, REDIS_ERROR_TYPE);
        } catch (Exception exception) {
            LOGGER.error("Global exception in filters." + exception);
            responseErrorMsg(httpResponse, "");
        }
    }

    /**
     * 返回redis不可用的错误消息
     *
     * @param httpServletResponse 返回体操作
     */
    private void responseErrorMsg(HttpServletResponse httpServletResponse, String serviceType) {
        String errorMsg = "操作失败，请检查服务依赖" + serviceType + "相关组件！";
        Result<Object> result = Result.ofFail(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMsg);
        String redisNotAvailableMsg = JSONObject.toJSONString(result);
        try {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            httpServletResponse.setCharacterEncoding(RESPONSE_CHAR_SET);
            httpServletResponse.setContentType(RESPONSE_CONTENT_TYPE);
            PrintWriter responseWriter = httpServletResponse.getWriter();
            responseWriter.write(redisNotAvailableMsg);
            responseWriter.flush();
        } catch (IOException ioException) {
            LOGGER.error("Return response message to client fail.", ioException);
        }
    }

    @Override
    public void destroy() {

    }
}
