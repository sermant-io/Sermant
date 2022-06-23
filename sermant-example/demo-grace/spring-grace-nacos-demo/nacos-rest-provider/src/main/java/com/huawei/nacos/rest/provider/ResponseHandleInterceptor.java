/*
 * Copyright (C) 2022-2022 Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.huawei.nacos.rest.provider;

import com.huawei.nacos.common.CommonConstants;
import com.huawei.nacos.common.HostUtils;
import com.huawei.nacos.rest.provider.stat.QpsUtils;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 响应拦截器
 *
 * @author zhouss
 * @since 2022-06-20
 */
public class ResponseHandleInterceptor implements HandlerInterceptor {
    @Value("${spring.application.name}")
    private String serviceName;

    @Autowired
    private Environment environment;

    private String ip;

    private final ThreadLocal<Entry> local = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        final Entry test = SphU.entry("test");
        local.set(test);
        final Integer qps = QpsUtils.get();
        QpsUtils.remove();
        response.addHeader(CommonConstants.QPS_KEY, String.valueOf(qps));
        response.addHeader(CommonConstants.IP_KEY, getIp());
        response.addHeader(CommonConstants.PORT_KEY, String.valueOf(request.getLocalPort()));
        response.addHeader(CommonConstants.SERVICE_NAME_KEY, serviceName);
        response.addHeader(CommonConstants.WARM_UP_STATE, getWarmUpState());
        return true;
    }

    private String getWarmUpState() {
        String property = environment.getProperty(CommonConstants.WARM_UP_ENVIRONMENT_AGG);
        if (property == null) {
            property = environment.getProperty(CommonConstants.WARM_UP_ENVIRONMENT);
        }
        if (property == null) {
            // 默认开启
            return "true";
        }
        return property;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (local.get() != null) {
            local.get().exit();
            local.remove();
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
    }

    private String getIp() {
        if (ip == null) {
            ip = HostUtils.getMachineIp();
        }
        return ip;
    }
}
