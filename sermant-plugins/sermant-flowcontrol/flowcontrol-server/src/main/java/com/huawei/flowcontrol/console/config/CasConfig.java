/*
 * Copyright (C) 2020-2021 Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.flowcontrol.console.config;

import com.huawei.flowcontrol.console.util.DataType;

import net.unicon.cas.client.configuration.CasClientConfigurerAdapter;
import net.unicon.cas.client.configuration.EnableCasClient;
import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.session.SingleSignOutHttpSessionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件名：CasConfig 版权： 描述：加载单点登录的过滤器、监听器
 *
 * @author Gaofang Wu
 * @since 2020-12-02 跟踪单号： 修改单号： 修改内容：修改忽略url
 */
@Configuration
@EnableCasClient
@ConditionalOnExpression("${conditional.cas.load}")
public class CasConfig extends CasClientConfigurerAdapter implements WebMvcConfigurer {
    private static final String IGNORE_PATTERN = "ignorePattern";

    /**
     * 忽略路径
     */
    @Value("#{'${ignorePattern}'.replace(',','|')}")
    private String ignorePattern;

    /**
     * cas服务端的地址
     */
    @Value("${cas.server-url-prefix}")
    private String casServerUrlPrefix;

    /**
     * 注入单点登录过滤器
     *
     * @return AuthenticationFilter 返回单点登录过滤器
     */
    @Bean
    public AuthenticationFilter authenticationFilter() {
        return new AuthenticationFilter();
    }

    @Override
    public void configureAuthenticationFilter(FilterRegistrationBean authenticationFilter) {
        super.configureAuthenticationFilter(authenticationFilter);

        // 配置忽略url
        authenticationFilter.getInitParameters().put(IGNORE_PATTERN, ignorePattern);
    }

    /**
     * 注入单点登出过滤器
     *
     * @return SingleSignOutFilter 返回单点登出过滤器
     */
    @Bean
    public SingleSignOutFilter singleSignOutFilter() {
        return new SingleSignOutFilter();
    }

    /**
     * 注入单点登出监听器
     *
     * @return SingleSignOutHttpSessionListener 返回单点登出监听器
     */
    @Bean
    public SingleSignOutHttpSessionListener singleSignOutHttpSessionListener() {
        return new SingleSignOutHttpSessionListener();
    }

    /**
     * 配置登出过滤器
     *
     * @return SingleSignOutFilter 返回登出过滤器
     */
    @Bean
    public FilterRegistrationBean logOutFilter() {
        FilterRegistrationBean authenticationFilter = new FilterRegistrationBean();
        authenticationFilter.setFilter(singleSignOutFilter());
        authenticationFilter.setEnabled(true);
        Map<String, String> initParameters = new HashMap<String, String>();
        initParameters.put("casServerUrlPrefix", casServerUrlPrefix);
        authenticationFilter.setInitParameters(initParameters);
        authenticationFilter.addUrlPatterns("/*");

        // 必须先加载登出过滤器
        authenticationFilter.setOrder(1);
        return authenticationFilter;
    }

    /**
     * 配置监听器
     *
     * @return SingleSignOutHttpSessionListener 返回单点登出监听器
     */
    @Bean
    public ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> singleSignOutHttpSessionListenerBean() {
        ServletListenerRegistrationBean<SingleSignOutHttpSessionListener> listenerRegistrationBean =
            new ServletListenerRegistrationBean<>();
        listenerRegistrationBean.setEnabled(true);
        listenerRegistrationBean.setListener(singleSignOutHttpSessionListener());
        listenerRegistrationBean.setOrder(Integer.parseInt(DataType.ORDER_TWO.getDataType()));
        return listenerRegistrationBean;
    }

    /**
     * 配置授权过滤器
     *
     * @return FilterRegistrationBean 返回注册过滤器
     */
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterRegistrationBean() {
        FilterRegistrationBean<AuthenticationFilter> authenticationFilter = new FilterRegistrationBean<>();
        authenticationFilter.setFilter(authenticationFilter());
        authenticationFilter.setInitParameters(Collections.singletonMap("ignorePattern", ignorePattern));
        authenticationFilter.setOrder(Integer.parseInt(DataType.ORDER_TWO.getDataType()));

        // 设置匹配的url
        authenticationFilter.setUrlPatterns(Collections.singletonList("/*"));
        return authenticationFilter;
    }
}
